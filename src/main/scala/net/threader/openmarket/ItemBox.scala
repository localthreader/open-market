package net.threader.openmarket

import com.google.common.collect.{ArrayListMultimap, Multimap}
import net.threader.openmarket.db.Database
import net.threader.openmarket.model.{ItemBoxItem, MarketItem}
import net.threader.openmarket.util.Util
import org.bukkit.{Bukkit, OfflinePlayer}
import org.bukkit.inventory.ItemStack

import java.sql.Connection
import java.time.ZoneId
import java.util.{Date, UUID}
import scala.collection.mutable
import scala.concurrent.ExecutionContext
import scala.util.Using

object ItemBox {
  val cached: Multimap[UUID, ItemBoxItem] = ArrayListMultimap.create()

  def load(): Unit = {
    cached.clear()
    Using(Database.connection.createStatement().executeQuery("SELECT * FROM item_box")) { rs =>
      while(rs.next()) {
        val holder = Bukkit.getOfflinePlayer(UUID.fromString(rs.getString("holder")))
        val id = UUID.fromString(rs.getString("id"))
        val stack = Util.fromB64(rs.getString("stack"))
        cached.put(holder.getUniqueId, ItemBoxItem(holder, id, stack))
      }
    }
  }

  def add(item: ItemBoxItem): Unit = asynConnection { conn =>
    cached.put(item.holder.getUniqueId, item)
    Using(conn.prepareStatement("INSERT INTO item_box VALUES (?, ?, ?)")) { statement =>
      statement.setString(1, item.holder.getUniqueId.toString)
      statement.setString(2, item.id.toString)
      statement.setString(3, Util.toB64(item.stack))
      statement.executeUpdate()
    }
  }

  def remove(item: ItemBoxItem): Unit = asynConnection { conn =>
    cached.remove(item.holder.getUniqueId, item)
    Using(conn.prepareStatement("DELETE FROM item_box WHERE id=?")) { statement =>
      statement.setString(1, item.id.toString)
      statement.executeUpdate()
    }
  }

  def asynConnection(block: Connection => Unit): Unit = {
    Bukkit.getScheduler.runTaskAsynchronously(OpenMarket.instance, new Runnable {
      override def run(): Unit = block(Database.connection)
    })
  }
}
