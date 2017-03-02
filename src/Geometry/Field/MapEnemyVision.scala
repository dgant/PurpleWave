package Geometry.Field

import Geometry.Circle
import Startup.With

class MapEnemyVision extends InfluenceMap {
  override def update() {
    reset()
    With.units.enemy.filter(_.possiblyStillThere).foreach(u => {
      val range = u.utype.sightRange / 32
      Circle.points(range)
        .foreach(tileDelta =>
          setTilePosition(
            u.tilePosition.getX + tileDelta._1 * 32,
            u.tilePosition.getY + tileDelta._2 * 32,
            1))
    })
  }
}