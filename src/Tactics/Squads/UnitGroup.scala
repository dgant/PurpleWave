package Tactics.Squads

import Information.Battles.Types.GroupCentroid
import Mathematics.Maff
import Performance.Cache
import ProxyBwapi.UnitInfo.UnitInfo

trait UnitGroup {
  def groupUnits: Seq[UnitInfo]

  def attackers       = _attackers()
  def detectors       = _detectors()
  def attacksAir      = _attacksAir()
  def attacksGround   = _attacksGround()
  def catchesAir      = _catchesAir()
  def catchesGround   = _catchesGround()
  def hasGround       = _hasGround()
  def engagingOn      = _engagingOn()
  def engagedUpon     = _engagedUpon()
  def centroidAll     = _centroidAll()
  def centroidAir     = _centroidAir()
  def centroidGround  = _centroidGround()
  def centroidKey     = _centroidKey()
  def battleConsensus = _battleConsensus()
  def threatConsensus = _threatConsensus()

  private def _attackers()      = groupUnits.view.filter(u => u.unitClass.canAttack  && ! u.unitClass.isWorker)
  private def _detectors()      = groupUnits.view.filter(u => u.aliveAndComplete && u.unitClass.isDetector)
  private val _attacksAir       = new Cache(() => attackers.exists(_.canAttackAir))
  private val _attacksGround    = new Cache(() => attackers.exists(_.canAttackGround))
  private val _catchesAir       = new Cache(() => attackers.exists(a => (a.pixelRangeAir > 96 || a.flying) && a.canAttackAir))
  private val _catchesGround    = new Cache(() => attackers.exists(a => (a.pixelRangeAir > 96 || a.flying) && a.canAttackGround))

  private val _hasGround        = new Cache(() => groupUnits.exists( ! _.flying))
  private val _engagingOn       = new Cache(() => groupUnits.exists(_.matchups.targetsInRange.nonEmpty))
  private val _engagedUpon      = new Cache(() => groupUnits.exists(_.matchups.pixelsOfEntanglement >= 0))
  private val _centroidAll      = new Cache(() => Maff.centroid(groupUnits.view.map(_.pixel)))
  private val _centroidAir      = new Cache(() => GroupCentroid.air(groupUnits))
  private val _centroidGround   = new Cache(() => GroupCentroid.ground(groupUnits))
  private val _centroidKey      = new Cache(() => if (_hasGround()) centroidGround else centroidAir )
  private val _battleConsensus  = new Cache(() => Maff.mode(groupUnits.view.map(_.battle)))
  private val _threatConsensus  = new Cache(() => battleConsensus.map(_.enemy.centroidGround))
}
