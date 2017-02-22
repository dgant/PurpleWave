package Geometry.Influence

import Geometry.Circle
import Startup.With

class MapGroundAttractors extends InfluenceMap {
  override def update() {
    reset()
    With.memory.knownEnemyUnits.filter(_.possiblyStillThere).foreach(u => {
      if ( ! u.getType.isFlyer && u.getHitPoints > 0) {
        val attraction = 10 * (u.getType.mineralPrice + u.getType.gasPrice) / (u.getHitPoints + u.getShields)
        val tileRadius = 8
        Circle.points(tileRadius).foreach(tileDelta =>
          addTilePosition(
            u.getTilePosition.getX + tileDelta._1,
            u.getTilePosition.getY + tileDelta._2,
            attraction / (1 + Math.abs(tileDelta._1) + Math.abs(tileDelta._2))))
      }
    })
  }
}
