package Macro.Architecture

import Lifecycle.With
import Mathematics.Points.Tile

import scala.util.Random

object Surveyor {
  
  def candidates(blueprint: Blueprint): Iterable[Tile] = {
    
    if (blueprint.townHall) {
      With.geography.bases
        .filterNot(base => base.owner.isEnemy || base.zone.island)
        .map(_.townHallArea.startInclusive)
    }
    else if (blueprint.gas) {
      With.geography.bases
        .filter(_.townHall.exists(_.player.isUs))
        .flatMap(_.gas.map(_.tileTopLeft))
    }
    else {
      Random.shuffle(
        With.geography.ourBases
          .flatMap(_.zone.tiles)
          .toList) ++
        Random.shuffle(
          With.geography.zones
            .filter(zone =>
              ! zone.island
                && zone.owner.isNeutral
                && With.geography.ourBases.exists(ourBase => ourBase.heart.groundPixels(zone.centroid) < 32.0 * 50.0))
            .toList
            .flatMap(_.tiles))
    }
  }
}
