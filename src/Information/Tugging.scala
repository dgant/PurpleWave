package Information

import Lifecycle.With
import Mathematics.Maff
import Mathematics.Points.Tile
import Performance.Cache
import ProxyBwapi.Races.Terran
import ProxyBwapi.UnitInfo.UnitInfo

trait Tugging {
  def tugDistance: Int = With.geography.home.tileDistanceGroundManhattan(With.scouting.mostBaselikeEnemyTile)
  def enemyProgress: Double = Maff.clamp(1 - enemyMuscleOrigin.tileDistanceGroundManhattan(With.geography.home).toDouble / tugDistance, 0, 1)
  def ourProgress: Double = Maff.clamp(1 - ourMuscleOrigin.tileDistanceGroundManhattan(With.scouting.mostBaselikeEnemyTile).toDouble / tugDistance, 0, 1)

  def threatOrigin: Tile = _threatOrigin()
  def enemyMuscleOrigin: Tile = _enemyMuscleOrigin()
  def ourMuscleOrigin: Tile = _ourMuscleOrigin()

  private val _threatOrigin = new Cache(() => {
    Maff.weightedExemplar(With.units.enemy.filter(countMuscle).map(u => (u.pixel, u.subjectiveValue)) ++ Seq((With.scouting.mostBaselikeEnemyTile.center, 5 * Terran.Marine.subjectiveValue))).nearestWalkableTile
  })

  private val _enemyMuscleOrigin = new Cache(() => {
    val muscle = With.units.enemy.filter(countMuscle)
    if (muscle.isEmpty) With.scouting.mostBaselikeEnemyTile.nearestWalkableTile
    else Maff.weightedExemplar(muscle.map(u => (u.pixel, u.subjectiveValue))).nearestWalkableTile
  })


  private val _ourMuscleOrigin = new Cache(() => {
    val muscle = With.units.ours.filter(countMuscle)
    if (muscle.isEmpty) With.geography.home
    else Maff.weightedExemplar(muscle.map(u => (u.pixel, u.subjectiveValue))).nearestWalkableTile
  })

  @inline private def countMuscle(u: UnitInfo): Boolean = {
    u.likelyStillThere && u.attacksAgainstGround > 0 && ! u.unitClass.isWorker && ! u.unitClass.isBuilding
  }
}
