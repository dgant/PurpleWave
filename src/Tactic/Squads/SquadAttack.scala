package Tactic.Squads

import Lifecycle.With
import Macro.Facts.MacroFacts
import Mathematics.Maff
import Mathematics.Points.{Pixel, Points, Tile}
import Mathematics.Shapes.Spiral
import Performance.Cache
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import ProxyBwapi.UnitTracking.UnorderedBuffer
import Tactic.Squads.AttackModes._
import Utilities.In
import Utilities.Time.Minutes
import Utilities.UnitFilters.IsBuilding

import scala.collection.mutable.ArrayBuffer

class SquadAttack extends Squad {

  var mode: AttackMode = PushMain

  override def launch(): Unit = {} // This squad receives its units from Tactician
  override def toString: String = f"$mode ${vicinity.base.map(_.name).getOrElse(vicinity.zone.name).take(4)}"

  private def faster: Boolean = MacroFacts.safeSkirmishing && With.self.speed > With.enemies.map(_.speed).max

  lazy val midmap: Cache[Option[Pixel]] = new Cache(() => Spiral(48)
    .map(Points.tileMiddle.add)
    .find(t => t.walkable && ! t.visibleToEnemy && t.enemyRange == 0 && ! t.zone.island)
    .map(_.center))

  private def setMode(newMode: AttackMode, newVicinity: Pixel, newTargets: => Iterable[UnitInfo]): Unit = {
    mode = newMode
    vicinity = newVicinity
    setTargets(newTargets) // The => in the function definition is key when using the vicinity to choose targets
  }

  def chooseMode(): AttackMode = {
    val proxies             = With.units.enemy.filter(_.proxied).toVector
    val enemyBasesOccupied  = units.view.flatMap(_.base).filter(_.isEnemy).toSet

    lazy val enemyBuildings       = With.units.enemy.filter(IsBuilding).filter(b => attacksAir || ! b.flying)
    lazy val nearestEnemyBuilding = Maff.minBy(enemyBuildings.map(_.pixel))(attackKeyDistanceTo)
    lazy val neutralBase          = Maff.minBy(With.geography.bases.filter(b => With.framesSince(b.lastFrameScoutedByUs) > Minutes(2)()))(b => attackKeyDistanceTo(b.heart.center))
    lazy val tilesSparse          = (0 until With.mapTileWidth by 4).flatMap(x => (0 until With.mapTileHeight by 4).map(y => Tile(x, y)).filter(t => With.framesSince(t.lastSeen) > Minutes(2)()))
    lazy val unseenTile           = Maff.minBy(tilesSparse)(t => attackKeyDistanceTo(t.center))
    lazy val unguardedBases       = With.geography.enemyBases.filter(b => attackKeyDistanceTo(b.heart.center) < b.groundPixels(With.scouting.enemyThreatOrigin)).sortBy(b => attackKeyDistanceTo(b.heart.center))

    if (With.yolo.active) {


      setMode(
        YOLO,
        nearestEnemyBuilding
          .orElse(neutralBase.map(_.heart.center))
          .orElse(unseenTile.map(_.center))
          .getOrElse(With.scouting.enemyHome.center),
        SquadAutomation.rankForArmy(this, With.units.enemy.filter(e => IsBuilding(e) || e.matchups.pixelsToThreatRange.exists(_ <= 0)).toVector))

    } else if (enemyBasesOccupied.nonEmpty) {

      setMode(
        RazeBase,
        enemyBasesOccupied.map(_.heart.center).minBy(attackKeyDistanceTo),
        SquadAutomation.rankedAround(this))

    } else if (proxies.nonEmpty) {

      setMode(
        RazeProxy,
        proxies.map(_.pixel).minBy(attackKeyDistanceTo),
        SquadAutomation.rankedAround(this))

    } else if (MacroFacts.killPotential) {

      setMode(
        PushMain,
        nearestEnemyBuilding.getOrElse(With.scouting.enemyHome.center),
        SquadAutomation.rankedEnRoute(this))

    } else if (MacroFacts.safePushing) {

      if (With.scouting.enemyProximity > 0.4) {

        setMode(
          CrushArmy,
          With.scouting.enemyMuscleOrigin.center,
          SquadAutomation.rankedEnRoute(this))

      } else if (unguardedBases.nonEmpty) {

        setMode(
          PushExpo,
          unguardedBases.head.heart.center,
          SquadAutomation.rankedAround(this))

      } else {

        setMode(
          ContainArmy,
          With.scouting.enemyThreatOrigin.center,
          SquadAutomation.rankedEnRoute(this))

      }
    } else if (faster && With.scouting.enemyProximity > 0.4) {

      setMode(
        Backstab,
        Maff.maxBy(With.geography.enemyBases.map(_.heart))(_.groundPixels(With.scouting.enemyMuscleOrigin)).getOrElse(With.scouting.enemyHome).center,
        SquadAutomation.rankedAround(this))

    } else {

      val target =
        Maff.orElse(
          Maff.orElse(
            With.geography.enemyBases.filterNot(b => With.scouting.enemyMain.exists(_.metro.bases.contains(b))),
            With.geography.enemyBases.filterNot(b => With.scouting.enemyMain.contains(b) || With.scouting.enemyNatural.contains(b)))
          .toSeq
          .sortBy(b => attackKeyDistanceTo(b.heart.center) - With.scouting.enemyThreatOrigin.groundPixels(b.heart)),
        With.geography.preferredExpansionsEnemy.filter(_.lastFrameScoutedByUs < With.frame - Minutes(1)()),
        With.geography.preferredExpansionsEnemy)
          .headOption
          .map(_.heart.center)
          .getOrElse(With.scouting.enemyThreatOrigin.center)

      setMode(
        ClearMap,
        target,
        SquadAutomation.rankedAround(this))
    }

    mode
  }

  val otherTargets = new ArrayBuffer[UnitInfo]
  private val unassigned = new UnorderedBuffer[FriendlyUnitInfo]()

  override def run(): Unit = {
    otherTargets.clear()

    if (units.isEmpty) return

    chooseMode()

    SquadAutomation.formAndSend(this)

    if (In(mode, ContainArmy, ClearMap) && MacroFacts.safeSkirmishing && units.size >= 12) {

      With.geography.preferredExpansionsEnemy
        .filterNot(_.island)
        .take(3)
        .foreach(base =>
        Maff.minBy(unintended.filter(_.canAttackGround))(_.framesToTravelTo(base.heart.center)).foreach(camper =>
          camper.intend(this)
            .setTerminus(base.heart.center)
            .setTargets(base.enemies)))
    }
  }
}
