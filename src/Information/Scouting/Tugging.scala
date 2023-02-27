package Information.Scouting

import Information.Geography.Types.{Base, Zone}
import Lifecycle.With
import Mathematics.Maff
import Mathematics.Points.{Pixel, Tile}
import Performance.Cache
import Planning.Predicates.MacroFacts
import ProxyBwapi.Races.Terran
import ProxyBwapi.UnitInfo.UnitInfo

trait Tugging {
  def tugStart                                  : Tile        = _tugStart()
  def tugEnd                                    : Tile        = _tugEnd()
  def tugLength                                 : Double      = tugStart.groundPixels(tugEnd)
  def tugDistanceGroundUs     (pixel  : Pixel)  : Double      = pixel.walkablePixel.groundPixels(tugStart)
  def tugDistanceGroundEnemy  (pixel  : Pixel)  : Double      = pixel.walkablePixel.groundPixels(tugEnd)
  def proximity               (pixel  : Pixel)  : Double      = Maff.clamp(0.5 + 0.5 * (tugDistanceGroundEnemy(pixel) - tugDistanceGroundUs(pixel)) / (tugDistanceGroundUs(pixel) + tugDistanceGroundEnemy(pixel)), 0, 1)
  def proximity               (tile   : Tile)   : Double      = proximity(tile.center)
  def proximity               (base   : Base)   : Double      = proximity(base.townHallArea.center)
  def ourProximity                              : Double      = _ourProximity()
  def enemyProximity                            : Double      = _enemyProximity()
  def weControl               (pixel  : Pixel)  : Boolean     = deltaMuscle11(pixel) >   0.75 || (ourProximity    <= proximity(pixel) && (enemyProximity < proximity(pixel) ||   MacroFacts.safePushing))
  def enemyControls           (pixel  : Pixel)  : Boolean     = deltaMuscle11(pixel) < - 0.75 || (enemyProximity  >= proximity(pixel) && (ourProximity   > proximity(pixel) || ! MacroFacts.safePushing))
  def weControl               (tile   : Tile)   : Boolean     = weControl(tile.center)
  def enemyControls           (tile   : Tile)   : Boolean     = enemyControls(tile.center)
  def weControlOurNatural                       : Boolean     = _weControlOurNatural()
  def weControl               (base   : Base)   : Boolean     = controlPoints(base).count(weControl) > controlPoints(base).count(enemyControls)
  def weControl               (zone   : Zone)   : Boolean     = controlPoints(zone).count(weControl) > controlPoints(zone).count(enemyControls)
  def enemyControls           (base   : Base)   : Boolean     = controlPoints(base).count(weControl) < controlPoints(base).count(enemyControls)
  def enemyControls           (zone   : Zone)   : Boolean     = controlPoints(zone).count(weControl) < controlPoints(zone).count(enemyControls)
  def ourMuscleDistance       (pixel  : Pixel)  : Double      = ourMuscleOrigin.groundPixels(pixel)
  def enemyMuscleDistance     (pixel  : Pixel)  : Double      = enemyMuscleOrigin.groundPixels(pixel)
  def deltaMuscle11           (pixel  : Pixel)  : Double      = (enemyMuscleDistance(pixel) - ourMuscleDistance(pixel)) / Math.max(13 * 32, enemyMuscleDistance(pixel) + ourMuscleDistance(pixel)) // Floor distance is to avoid wild swings when both players are close
  private def controlPoints   (base   : Base)   : Seq[Pixel]  = Seq(Some(base.townHallArea.center), Some(base.centroid.center), base.zone.entranceNow.map(_.pixelCenter), base.zone.exitNow.map(_.pixelCenter)).flatten
  private def controlPoints   (zone   : Zone)   : Seq[Pixel]  = (zone.bases.flatMap(controlPoints) ++ Seq(Some(zone.centroid.center), zone.entranceNow.map(_.pixelCenter), zone.exitNow.map(_.pixelCenter)).flatten).distinct

  def enemyThreatOrigin : Tile = _enemyThreatOrigin()
  def enemyMuscleOrigin : Tile = _enemyMuscleOrigin()
  def ourThreatOrigin   : Tile = _ourThreatOrigin()
  def ourMuscleOrigin   : Tile = _ourMuscleOrigin()

  private val _tugStart             = new Cache(() => With.geography.home)
  private val _tugEnd               = new Cache(() => With.scouting.enemyHome)
  private val _ourProximity         = new Cache(() => proximity(ourMuscleOrigin))
  private val _enemyProximity       = new Cache(() => proximity(enemyMuscleOrigin))
  private val _weControlOurNatural  = new Cache(() => weControl(With.geography.ourNatural))

  private lazy val productionWeight = 5 * Terran.Marine.subjectiveValue
  private val _enemyThreatOrigin = new Cache(() => {
    Maff.weightedExemplar(With.units.enemy.filter(countMuscle).map(u => (u.pixel, u.subjectiveValue)) ++ Seq((With.scouting.enemyHome.center, productionWeight))).walkableTile
  })

  private val _ourThreatOrigin = new Cache(() => {
    Maff.weightedExemplar(
      With.units.ours.filter(countMuscle).map(u => (u.pixel, u.subjectiveValue))
      ++ Seq((With.geography.home.center, productionWeight))).walkableTile
  })

  private val _enemyMuscleOrigin = new Cache(() => {
    val muscle = With.units.enemy.filter(countMuscle)
    if (muscle.isEmpty) With.scouting.enemyHome.walkableTile
    else Maff.weightedExemplar(muscle.map(u => (u.pixel, u.subjectiveValue))).walkableTile
  })

  private val _ourMuscleOrigin = new Cache(() => {
    val muscle = With.units.ours.filter(countMuscle)
    if (muscle.size < 7) _ourThreatOrigin() // Small unit counts tend to produce high oscillation
    else Maff.weightedExemplar(muscle.map(u => (u.pixel, u.subjectiveValue))).walkableTile
  })

  @inline private def countMuscle(u: UnitInfo): Boolean = {
    u.likelyStillThere && u.attacksAgainstGround > 0 && ! u.unitClass.isWorker && ! u.unitClass.isBuilding
  }
}
