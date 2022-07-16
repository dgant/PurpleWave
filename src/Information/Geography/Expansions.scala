package Information.Geography

import Information.Geography.Pathfinding.PathfindProfile
import Information.Geography.Pathfinding.Types.TilePath
import Information.Geography.Types.Base
import Lifecycle.With
import Mathematics.Maff
import Mathematics.Points.Tile
import ProxyBwapi.Players.{PlayerInfo, Players}
import bwapi.Race

trait Expansions {
  private var _safeExpansionPaths: Vector[(Base, Tile, TilePath)] = Vector.empty
  private var _safeExpansions : Vector[Base] = Vector.empty
  private var _preferredOurs  : Vector[Base] = Vector.empty
  private var _preferredEnemy : Vector[Base] = Vector.empty

  def preferredExpansionsOurs   : Vector[Base] = _preferredOurs
  def preferredExpansionsEnemy  : Vector[Base] = _preferredEnemy
  def safeExpansions            : Vector[Base] = _safeExpansions

  def eligibleExpansions(player: PlayerInfo): Iterable[Base] = {
    val enemies = player.enemies.toVector
    Maff.orElse(With.geography.neutralBases, With.geography.bases.filter(b => enemies.contains(b.owner)))
  }

  protected def updateExpansions(): Unit = {
    _safeExpansionPaths = eligibleExpansions(With.self)
      .filter(_.isNeutral)
      .map(b => (b, Maff.orElse(With.geography.ourBases.map(_.heart), Seq(With.geography.home)).minBy(_.groundPixels(b.heart))))
      .map(bh => (bh._1, bh._2, {
          val profile = new PathfindProfile(bh._2, Some(bh._1.heart))
          profile.threatMaximum = Some(0)
          profile.employGroundDist = true
          profile.lengthMaximum = Some(2 * bh._2.groundTiles(bh._1.heart))
          profile.find
        }))
      .toVector
    _safeExpansions = _safeExpansionPaths.filter(_._3.pathExists).map(_._1)
    _preferredOurs  = rankForPlayer(With.self)
    _preferredEnemy = Maff.maxBy(With.enemies)(_.supplyUsed400).map(rankForPlayer).getOrElse(Vector.empty)
    _safeExpansions = _preferredOurs.filter(_safeExpansions.contains)
  }

  private def rankForPlayer(player: PlayerInfo): Vector[Base] = {
    val totalBases      = With.geography.bases.count(b => b.owner == player)
    val gasBases        = With.geography.bases.count(b => b.owner == player && adequateGas(b))
    val tileHome        = if (player.isFriendly)  With.geography.home               else With.scouting.enemyHome
    val tileEnemy       = if (player.isEnemy)     With.geography.home               else With.scouting.enemyHome
    val friendlyPlayers = if (player.isFriendly)  Players.all.filter(_.isFriendly)  else With.enemies
    val opposingPlayers = if (player.isEnemy)     Players.all.filter(_.isFriendly)  else With.enemies
    val playerBases     = With.geography.bases.filter(b => player == b.owner)
    val enemyBases      = With.geography.bases.filter(b => opposingPlayers.exists(_ == b.owner))
    val friendlyTiles   = Maff.orElse(playerBases .map(_.heart), Seq(tileHome))
    val opposingTiles   = Maff.orElse(With.geography.bases.filter(b => opposingPlayers.exists(_ == b.owner)).map(_.heart), Seq(tileEnemy))
    val opposingRaces   = opposingPlayers.map(_.raceCurrent).filterNot(Race.Unknown==)

    val raceWeights     = weightsTowards  .getOrElse(player.raceCurrent, Map.empty)
    val raceGasBases    = gasNeeds        .getOrElse(player.raceCurrent, Map.empty)
    val weightTowards   = Maff.min(opposingRaces.flatMap(raceWeights.get)).getOrElse(-0.75)
    val gasBasesNeeded  = Maff.max(opposingRaces.flatMap(raceGasBases.get)).getOrElse(3)

    def scoreBase(base: Base): Double = {
      val distanceHome  = Maff.mean(friendlyTiles.map(base.heart.groundPixels)) / 32
      val distanceEnemy = Maff.mean(opposingTiles.map(base.heart.groundPixels)) / 32
      val homeFactor    = Maff.clamp(1.0 - distanceHome   / 256.0,  0.1, 1.0)
      val enemyFactor   = Maff.clamp(1.0 - distanceEnemy  / 256.0,  0.1, 1.0)
      val naturalFactor = if (base.naturalOf.exists(_.owner == player) || base.natural.exists(_.owner == player)) 100.0 else 1.0
      val gasFactor     = if (adequateGas(base) || gasBases > gasBasesNeeded) 1.0 else if (gasBases == gasBasesNeeded) 0.75 else 0.1
      val safeFactor    = if (_safeExpansions.contains(base)) 1.0 else 0.2
      val output        = homeFactor * naturalFactor * gasFactor + enemyFactor * weightTowards
      output
    }

    val scores = eligibleExpansions(player).map(b => (b, scoreBase(b))).toVector.sortBy(- _._2)
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

  private val weightsTowards = Map[Race, Map[Race, Double]](
    (Race.Terran, Map[Race, Double](
      (Race.Terran,   -0.75),
      (Race.Protoss,  -0.25),
      (Race.Zerg,     -0.25))),
    (Race.Protoss, Map[Race, Double](
      (Race.Terran,   -0.75),
      (Race.Protoss,  -0.75),
      (Race.Zerg,     -0.25))),
    (Race.Zerg, Map[Race, Double](
      (Race.Terran,   -0.75),
      (Race.Protoss,  -0.75),
      (Race.Zerg,     -0.75))))

}
