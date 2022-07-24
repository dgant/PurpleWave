package Information.Scouting

import Information.Geography.Types.Base
import Lifecycle.With
import ProxyBwapi.Races.{Terran, Zerg}
import Utilities.Time.{Forever, GameTime, Minutes, Seconds}

trait BaseInference {
  def firstEnemyMain            : Option[Base]  = _firstEnemyMain
  def enemyMain                 : Option[Base]  = _firstEnemyMain.filter(base => ! base.scoutedByUs || base.owner.isEnemy)
  def enemyNatural              : Option[Base]  = enemyMain.flatMap(_.natural)
  def firstExpansionFrameEnemy  : Int           = _firstExpansionFrameEnemy
  def firstExpansionFrameUs     : Int           = _firstExpansionFrameUs
  def weExpandedFirst           : Boolean       = _firstExpansionFrameUs < _firstExpansionFrameEnemy
  def enemyExpandedFirst        : Boolean       = _firstExpansionFrameEnemy < _firstExpansionFrameUs
  def enemyMainFullyScouted     : Boolean       = _enemyMainScouted
  private var _firstEnemyMain: Option[Base] = None
  private var _firstExpansionFrameEnemy: Int = Forever()
  private var _firstExpansionFrameUs: Int = Forever()
  private var _enemyMainScouted: Boolean = false

  def enemyNaturalPossiblyMining: Boolean = {
    // Recent observations are proof
    var output = enemyNatural.forall(b => With.framesSince(b.lastFrameScoutedByUs) > Terran.CommandCenter.buildFrames)
    // Infer
    output &&= ! (With.fingerprints.twoGate()       && With.frame < GameTime(5, 35)() && ! With.fingerprints.twoGate99()) // 10-12 only
    output &&= ! (With.fingerprints.dtRush()        && With.frame < GameTime(5, 40)())
    output &&= ! (With.fingerprints.oneGateCore()   && With.frame < GameTime(5, 15)())
    output &&= ! (With.fingerprints.twoGateGoon()   && With.frame < GameTime(6,  0)()) // Haven't actually timed this one; just interpolating
    output &&= ! (With.fingerprints.threeGateGoon() && With.frame < GameTime(6, 45)())
    output &&= ! (With.fingerprints.fourGateGoon()  && With.frame < GameTime(7,  5)())
    output &&= With.sense.enemySecretMinerals >= 400
    // Accept obvious proof
    output ||= enemyNatural.exists(_.townHall.exists(_.complete))
    output ||= enemyNatural.exists(_.owner.isEnemy)
    output
  }

  protected def updateBaseInference(): Unit = {
    _firstEnemyMain = _firstEnemyMain.orElse(With.geography.startBases.find(_.owner.isEnemy))
    _firstEnemyMain = _firstEnemyMain.orElse(With.geography.startBases.view.filter(_.owner.isNeutral).find(_.naturalOf.exists(_.owner.isEnemy)))
    if (_firstEnemyMain.isEmpty) {
      val possibleMains = With.geography.startBases.filterNot(_.owner.isUs).filter(base => base.owner.isEnemy || ! base.scoutedByUs)
      // Infer possible mains from process of elimination
      if (possibleMains.size == 1) {
        _firstEnemyMain = possibleMains.headOption
        With.logger.debug("Inferred enemy main from process of elimination.")
      }
      // Infer main by creep
      else if (With.frame < Minutes(5)() && With.enemies.exists(_.isZerg)) {
        val newlyCreepedBases = With.geography.startBases.filter(b => b.owner.isNeutral && b.zone.tiles.exists(_.creep))
        if (newlyCreepedBases.size == 1) {
          _firstEnemyMain = newlyCreepedBases.headOption
          With.logger.debug("Inferred enemy main from presence of creep: " + _firstEnemyMain.get.toString)
        }
      }
      // Infer main by Overlord
      else if (With.frame < GameTime(2, 30)()) {
        val overlords = With.units.enemy.filter(Zerg.Overlord)
        val overlordMains = overlords.map(overlord => (overlord, With.geography.startBases.filter(base =>
          base.owner.isNeutral
          && overlord.framesToTravelTo(base.townHallArea.center) < With.frame + Seconds(5)()
        )))
        val overlordProofs = overlordMains.find(_._2.size == 1)
        overlordProofs.foreach(overlordProof => {
          _firstEnemyMain = overlordProof._2.headOption
          With.logger.debug("Inferred enemy main from Overlord position: " + overlordProof._1 + " -> " + _firstEnemyMain.get.toString)
        })
      }
    }

    if (With.geography.ourBases.size > 1 && _firstExpansionFrameUs == Forever()) {
      With.logger.debug(f"Recording our first expansion frame")
      _firstExpansionFrameUs = With.frame
    }
    if (With.enemies.exists(_.bases.size > 1) && _firstExpansionFrameEnemy == Forever()) {
      With.logger.debug(f"Recording first enemy expansion frame")
      _firstExpansionFrameEnemy = With.frame
    }
  }
  if (_firstEnemyMain.isDefined && ! _enemyMainScouted) {
    val main = _firstEnemyMain.get
    val scoutableTiles = main.zone.tiles.view
      .filter(With.grids.buildableTerrain.get)
      .filter(t =>
        ! main.owner.isZerg
        || main.townHallTile.x - t.x < 8
        || main.townHallTile.x - t.x > -11
        || main.townHallTile.y - t.y < 5
        || main.townHallTile.y - t.y > -7)
    val tilesSeen = scoutableTiles.count(_.explored)
    _enemyMainScouted = tilesSeen >= scoutableTiles.size * 0.9
  }
}
