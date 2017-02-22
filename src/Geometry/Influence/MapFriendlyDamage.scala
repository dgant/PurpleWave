package Geometry.Influence

import Startup.With
import bwapi.{Position, UnitType}

import scala.collection.JavaConverters._

abstract class MapFriendlyDamage extends MapDamage {
  override def getUnits:Iterable[(Position, UnitType)] = {
    (With.game.allies.asScala :+ With.game.self).flatten(_.getUnits.asScala).map(unit => (unit.getPosition, unit.getType))
  }
}
