package Debugging.Visualizations.Views.Economy

import Debugging.Visualizations.Rendering.DrawScreen
import Debugging.Visualizations.Views.DebugView
import Lifecycle.With

object ShowSupply extends DebugView {
  override def renderScreen(): Unit = {
    val supplier = With.supplier
    DrawScreen.table(340, 45, Seq(
      Seq("Supply used",          f"        ${supplier.simSupplyUsed / 2}", "/ 200"),
      Seq("Supply from halls",    f"        ${supplier.simSupplyHalls / 2}", "/ 200"),
      Seq("Supply deficit",       f"        ${(supplier.simSupplyUsed - supplier.simSupplyHalls) / 2}", "/ 200"),
      Seq(),
      Seq("Depots now",           f"        ${With.units.countOurs(supplier.depot)}"),
      Seq("Depots needed",        f"        ${supplier.simDepots}"),
      Seq(),
      Seq("Depots demanded now",  f"        ${With.scheduler.requests.find(_._1 == With.supplier).map(_._2.count(_.minFrame <= 0)).getOrElse(0)}")
    ))
  }
}
