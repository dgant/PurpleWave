package Debugging.Visualizations.Views.Economy

import Debugging.Visualizations.Rendering.DrawScreen
import Debugging.Visualizations.Views.DebugView
import Lifecycle.With
import Utilities.Time.Frames

object ShowSupply extends DebugView {
  override def renderScreen(): Unit = {
    val supplier = With.supplier
    val requested = With.scheduler.requests.find(_._1 == supplier).map(_._2.toVector).getOrElse(Vector.empty)
    DrawScreen.table(340, 45,
      Seq(
        Seq("Supply used",        f"        ${supplier.simSupplyUsed / 2}", "/ 200"),
        Seq("Supply from halls",  f"        ${supplier.simSupplyHalls / 2}", "/ 200"),
        Seq("Supply deficit",     f"        ${(supplier.simSupplyUsed - supplier.simSupplyHalls) / 2}", "/ 200"),
        Seq(),
        Seq("Farms now",          f"        ${With.units.countOurs(supplier.farm)}"),
        Seq("Farms needed",       f"        ${supplier.simFarms}"),
        Seq("Farms demanded now", f"        ${With.scheduler.requests.find(_._1 == With.supplier).map(_._2.count(_.minStartFrame <= With.frame)).getOrElse(0)}"),
        Seq())
      ++ Seq("Farms queued")
        .padTo(requested.length, "")
        .take(requested.length)
        .zipWithIndex
        .map(p => Seq(p._1,       f"        ${Frames(Math.max(0, requested(p._2).minStartFrame - With.frame))}"))
      ++ Seq("Eaters")
        .padTo(supplier.consumed.length, "")
        .take(supplier.consumed.length)
        .zipWithIndex
        .map(p => Seq(p._1,       f"        ${supplier.consumed(p._2)._2} in ${Frames(supplier.consumed(p._2)._1)}"))
    )
  }
}
