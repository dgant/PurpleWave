package Debugging.Visualizations.Views.Planning

import Debugging.Visualizations.Views.View
import Lifecycle.With

object ShowPreplacement extends View {

  override def renderMap(): Unit = {
    With.preplacement.fits.foreach(_.drawMap())

    val zone = With.geography.ourMain.zone
    val exit = zone.exit.map(_.pixelCenter.tileIncluding)
    if(exit.isEmpty) return

    /*
    val back = zone.tiles.maxBy(_.tileDistanceFast(exit.get))
    val generator = new TileGenerator(exit.get, zone.boundary.startInclusive, zone.boundary.endExclusive, new Direction(exit.get, back))
    var previous = exit.get
    var next = previous
    var i = 0
    while (generator.hasNext) {
      next = generator.next()
      DrawMap.arrow(previous.pixelCenter, next.pixelCenter, Colors.NeonOrange)
      DrawMap.label(i.toString, next.pixelCenter.add(0, 7))
      i += 1
      previous = next
    }
    */
  }
}
