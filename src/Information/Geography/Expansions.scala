package Information.Geography

import Information.Geography.Pathfinding.PathfindProfile
import Information.Geography.Pathfinding.Types.TilePath
import Information.Geography.Types.Base
import Lifecycle.With
import Macro.Facts.MacroFacts
import Mathematics.Maff
import Mathematics.Points.Tile
import ProxyBwapi.Players.PlayerInfo
import ProxyBwapi.Races.Protoss
import Utilities.?
import bwapi.Race

trait Expansions {

  private var _maxMiningBasesOurs: Int = 1
  private var _maxMiningBasesEnemy: Int = 1
  private var _safeExpansionPaths: Vector[(Base, Tile, TilePath)] = Vector.empty
  private var _safeExpansions : Vector[Base] = Vector.empty
  private var _preferredOurs  : Vector[Base] = Vector.empty
  private var _preferredEnemy : Vector[Base] = Vector.empty

  def maxMiningBasesOurs        : Int          = _maxMiningBasesOurs
  def maxMiningBasesEnemy       : Int          = _maxMiningBasesEnemy
  def preferredExpansionsOurs   : Vector[Base] = _preferredOurs
  def preferredExpansionsEnemy  : Vector[Base] = _preferredEnemy
  def safeExpansions            : Vector[Base] = _safeExpansions

  def eligibleExpansions(player: PlayerInfo): Iterable[Base] = {
    val enemies = player.enemies.toVector
    Maff.orElse(With.geography.neutralBases, With.geography.bases.filter(b => enemies.contains(b.owner)))
  }

  protected def updateExpansions(): Unit = {
    _maxMiningBasesOurs = Math.max(_maxMiningBasesOurs, With.geography.ourMiningBases.size)
    _maxMiningBasesEnemy = Math.max(_maxMiningBasesOurs, With.geography.enemyMiningBases.size)
    _safeExpansionPaths = eligibleExpansions(With.self)
      .filter(_.isNeutral)
      .map(b => (b, Maff.orElse(With.geography.ourBases.map(_.heart), Seq(With.geography.home)).minBy(_.groundPixels(b.heart))))
      .map(bh => (bh._1, bh._2, {
          val profile = new PathfindProfile(bh._2, Some(bh._1.heart))
          profile.threatMaximum       = Some(0)
          profile.employGroundDist    = true
          profile.endDistanceMaximum  = 32 + bh._2.groundTiles(bh._1.heart)
          profile.lengthMaximum       = Some(64)
          profile.find
        }))
      .toVector
    _safeExpansions = _safeExpansionPaths.filter(_._3.pathExists).map(_._1)
    _preferredOurs  = rankForPlayer(With.self)
    _preferredEnemy = Maff.maxBy(With.enemies)(_.supplyUsed400).map(rankForPlayer).getOrElse(Vector.empty)
    _safeExpansions = _preferredOurs.filter(_safeExpansions.contains)
  }

  private def distanceToMultiplier(value: Double): Double = 1.0 - Maff.clamp(value * Maff.inv256, 0.0, 0.75)
  private def rankForPlayer(player: PlayerInfo): Vector[Base] = {
    val totalBases      = With.geography.bases.count(b => b.owner == player)
    val gasBases        = With.geography.bases.count(b => b.owner == player && adequateGas(b))
    val tileHome        = ?(player.isFriendly,  With.geography.home,     With.scouting.enemyHome)
    val tileEnemy       = ?(player.isEnemy,     With.scouting.enemyHome, With.geography.home)
    val friendlyPlayers = ?(player.isFriendly,  Vector(player),          With.enemies)
    val opposingPlayers = ?(player.isEnemy,     Vector(player),          With.enemies)
    val friendlyBases   = With.geography.bases.filter(b => friendlyPlayers.contains(b.owner))
    val opposingBases   = With.geography.bases.filter(b => opposingPlayers.contains(b.owner))
    val friendlyTiles   = Maff.orElse(friendlyBases.map(_.heart), Seq(tileHome))
    val opposingTiles   = Maff.orElse(opposingBases.map(_.heart), Seq(tileEnemy))
    val opposingRaces   = opposingPlayers.map(_.raceCurrent).filterNot(Race.Unknown==)

    val raceShyness     = matchupShyness  .getOrElse(player.raceCurrent, Map.empty)
    val raceGasBases    = gasNeeds        .getOrElse(player.raceCurrent, Map.empty)
    val shyness         = Maff.min(opposingRaces.flatMap(raceShyness.get)).getOrElse(1.0)
    val gasBasesNeeded  = Maff.max(opposingRaces.flatMap(raceGasBases.get) ++ Seq(4).filter(x => With.unitsShown(player, Protoss.FleetBeacon) > 0)).getOrElse(3)

    def scoreBase(base: Base, player: PlayerInfo): Double = {
      val sameMetro         = ?(base.metro.bases.exists(_.owner == player), 10, 1)
      val originStrength    = ?(player.isFriendly,  With.scouting.ourMuscleOrigin, With.scouting.enemyMuscleOrigin)
      val originThreat      = ?(player.isEnemy,     With.scouting.ourMuscleOrigin, With.scouting.enemyMuscleOrigin)
      val distanceHome      = Maff.mean(friendlyTiles.map(base.heart.groundTiles).map(_.toDouble))
      val distanceEnemy     = Maff.mean(opposingTiles.map(base.heart.groundTiles).map(_.toDouble))
      val distanceStrength  = originStrength.groundTiles(base.heart)
      val distanceThreat    = originThreat.groundTiles(base.heart)
      val nearHome          = distanceToMultiplier(distanceHome)
      val nearEnemy         = distanceToMultiplier(distanceEnemy)
      val nearStrength      = distanceToMultiplier(distanceStrength)
      val nearThreat        = distanceToMultiplier(distanceThreat)
      val factorNatural     = ?(base.naturalOf.exists(_.owner == player) || base.natural.exists(_.owner == player), 1000.0, 1.0)
      val factorGas         = ?(adequateGas(base) || gasBases > gasBasesNeeded || player.gas > 800, 1.0, ?(gasBases == gasBasesNeeded, 0.75, 0.1))
      val factorSafe        = ?(_safeExpansions.contains(base) || player.isEnemy, 1.0, 0.2)
      val factorFullness    = ?(MacroFacts.isMiningBase(base), 1.0, 0.1)
      val output            = sameMetro * nearHome * nearStrength * factorNatural * factorGas * factorSafe * factorFullness - nearEnemy * nearThreat * shyness
      output
    }

    val scores = eligibleExpansions(player).map(b => (b, scoreBase(b, player))).toVector.sortBy(- _._2)
    scores.map(_._1)
  }

  private def adequateGas(base: Base): Boolean = base.gas.map(_.gasLeft).sum > 1000

  private val gasNeeds = Map[Race, Map[Race, Int]](
    (Race.Terran, Map[Race, Int](
      (Race.Terran,   3),
      (Race.Protoss,  3),
      (Race.Zerg,     2))),
    (Race.Protoss, Map[Race, Int](
      (Race.Terran,   3),
      (Race.Protoss,  3),
      (Race.Zerg,     2))),
    (Race.Zerg, Map[Race, Int](
      (Race.Terran,   4),
      (Race.Protoss,  3),
      (Race.Zerg,     5))))

  private val matchupShyness = Map[Race, Map[Race, Double]](
    (Race.Terran, Map[Race, Double](
      (Race.Terran,   1.25),
      (Race.Protoss,  0.75),
      (Race.Zerg,     0.75))),
    (Race.Protoss, Map[Race, Double](
      (Race.Terran,   1.5),
      (Race.Protoss,  1.25),
      (Race.Zerg,     0.75))),
    (Race.Zerg, Map[Race, Double](
      (Race.Terran,   0.75),
      (Race.Protoss,  0.75),
      (Race.Zerg,     0.75))))

}
