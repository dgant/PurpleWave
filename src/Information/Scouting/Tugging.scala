package Information.Scouting

import Information.Geography.Types.Base
import Lifecycle.With
import Mathematics.Maff
import Mathematics.Points.{Pixel, Tile}
import Performance.Cache
import Planning.Predicates.MacroFacts
import ProxyBwapi.Races.Terran
import ProxyBwapi.UnitInfo.UnitInfo
import Utilities.?

trait Tugging {
  def tugStart                                  : Tile    = With.geography.home
  def tugEnd                                    : Tile    = With.scouting.enemyHome
  def tugLength                                 : Int     = tugStart.groundTiles(With.scouting.enemyHome)
  def tugDistanceGroundUs     (pixel  : Pixel)  : Double  = pixel.walkablePixel.groundPixels(tugStart)
  def tugDistanceGroundEnemy  (pixel  : Pixel)  : Double  = pixel.walkablePixel.groundPixels(tugEnd)
  def proximity               (pixel  : Pixel)  : Double  = Maff.clamp(0.5 + 0.5 * (tugDistanceGroundEnemy(pixel) - tugDistanceGroundUs(pixel)) / (tugDistanceGroundUs(pixel) + tugDistanceGroundEnemy(pixel)), 0, 1)
  def proximity               (tile   : Tile)   : Double  = proximity(tile.center)
  def proximity               (base   : Base)   : Double  = proximity(base.townHallArea.center)
  def ourProximity                              : Double  = proximity(ourMuscleOrigin)
  def enemyProximity                            : Double  = proximity(enemyMuscleOrigin)
  def weControl               (pixel  : Pixel)  : Boolean = ourProximity    < proximity(pixel) && (enemyProximity < proximity(pixel) ||   MacroFacts.safeToMoveOut)
  def enemyControls           (pixel  : Pixel)  : Boolean = enemyProximity  > proximity(pixel) && (ourProximity   > proximity(pixel) || ! MacroFacts.safeToMoveOut)
  def weControl               (tile   : Tile)   : Boolean = weControl(tile.center)
  def enemyControls           (tile   : Tile)   : Boolean = enemyControls(tile.center)
  def weControl               (base   : Base)   : Boolean = weControl(base.townHallArea.center) || weControl(base.centroid) || ?(proximity(base) > 0.5, base.zone.exitNow, base.zone.entranceNow).map(_.pixelCenter).exists(weControl)
  def weControlOurNatural                       : Boolean = weControl(With.geography.ourNatural)

  def enemyThreatOrigin : Tile = _enemyThreatOrigin()
  def enemyMuscleOrigin : Tile = _enemyMuscleOrigin()
  def ourThreatOrigin   : Tile = _ourThreatOrigin()
  def ourMuscleOrigin   : Tile = _ourMuscleOrigin()

  private lazy val productionWeight = 5 * Terran.Marine.subjectiveValue
  private val _enemyThreatOrigin = new Cache(() => {
    Maff.weightedExemplar(With.units.enemy.filter(countMuscle).map(u => (u.pixel, u.subjectiveValue)) ++ Seq((With.scouting.enemyHome.center, productionWeight))).walkableTile
  })

  private val _ourThreatOrigin = new Cache(() => {
    Maff.weightedExemplar(With.units.ours.filter(countMuscle).map(u => (u.pixel, u.subjectiveValue)) ++ Seq((With.geography.home.center, productionWeight))).walkableTile
  })

  private val _enemyMuscleOrigin = new Cache(() => {
    val muscle = With.units.enemy.filter(countMuscle)
    if (muscle.isEmpty) With.scouting.enemyHome.walkableTile
    else Maff.weightedExemplar(muscle.map(u => (u.pixel, u.subjectiveValue))).walkableTile
  })

  private val _ourMuscleOrigin = new Cache(() => {
    val muscle = With.units.ours.filter(countMuscle)
    if (muscle.isEmpty) With.geography.home
    else Maff.weightedExemplar(muscle.map(u => (u.pixel, u.subjectiveValue))).walkableTile
  })

  @inline private def countMuscle(u: UnitInfo): Boolean = {
    u.likelyStillThere && u.attacksAgainstGround > 0 && ! u.unitClass.isWorker && ! u.unitClass.isBuilding
  }
}
