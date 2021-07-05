package net.threader.openmarket.ui.market

import org.bukkit.entity.Player
import net.threader.openmarket.Market
import net.threader.openmarket.model.MarketItem
import net.threader.openmarket.ui.SimpleGUI

import java.util
import java.util.concurrent.atomic.AtomicInteger
import scala.collection.mutable
import scala.collection.mutable.{ArrayBuffer, ListBuffer}

case class MarketUI(player: Player, initialPage: Int) {
  val pages = new util.ArrayList[MarketPageUI]()
  val iterator = pages.listIterator(initialPage)

  var count = new AtomicInteger(0)
  Market.cached forEach { (seller, item) =>
    var items = ArrayBuffer[MarketItem]()
    if (count.incrementAndGet() > 36) {
      items += item
    } else {
      pages.add(MarketPageUI(player, this, items))
      items = new ArrayBuffer[MarketItem]()
    }

    def open(): Unit = {

    }
  }

}
