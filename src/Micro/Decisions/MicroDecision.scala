package Micro.Decisions

import ProxyBwapi.UnitInfo.FriendlyUnitInfo

abstract class MicroDecision(actingAgent: FriendlyUnitInfo) {
  
  lazy val evaluation: Double = MicroOptions.evaluate(this)
  val currentPixel = actingAgent.pixelCenter
  
  def agent: FriendlyUnitInfo = actingAgent
  def valueFixed: Double
  def valuePerFrame: Double
  def frames: Double
  def legal: Boolean
  def execute()
  def renderMap() {}
}
