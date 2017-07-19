package Micro.Decisions

import ProxyBwapi.UnitInfo.FriendlyUnitInfo

abstract class MicroDecision(actingAgent: FriendlyUnitInfo) {
  
  def agent: FriendlyUnitInfo = actingAgent
  def valueFixed: Double
  def valuePerFrame: Double
  def frames: Double
  def legal: Boolean
  def execute()
}
