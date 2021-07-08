package Tactics.Squads

import Information.Battles.Types.GroupCentroid
import Mathematics.Maff
import Performance.Cache
import ProxyBwapi.UnitInfo.UnitInfo

trait GroupConsensus {
  protected def consensusUnits: Seq[UnitInfo]

  def hasGround       = _hasGround()
  def engagedUpon     = _engagedUpon()
  def centroidAll     = _centroidAll()
  def centroidAir     = _centroidAir()
  def centroidGround  = _centroidGround()
  def centroidKey     = _centroidKey()
  def battleConsensus = _battleConsensus()
  def threatConsensus = _threatConsensus()

  private val _hasGround        = new Cache(() => consensusUnits.exists( ! _.flying))
  private val _engagedUpon      = new Cache(() => consensusUnits.exists(_.matchups.pixelsOfEntanglement >= 0))
  private val _centroidAll      = new Cache(() => Maff.centroid(consensusUnits.view.map(_.pixel)))
  private val _centroidAir      = new Cache(() => GroupCentroid.air(consensusUnits))
  private val _centroidGround   = new Cache(() => GroupCentroid.ground(consensusUnits))
  private val _centroidKey      = new Cache(() => if (_hasGround()) centroidGround else centroidAir )
  private val _battleConsensus  = new Cache(() => Maff.mode(consensusUnits.view.map(_.battle)))
  private val _threatConsensus  = new Cache(() => battleConsensus.map(_.enemy.centroidGround))
}
