package Micro.Squads

import ProxyBwapi.UnitInfo.FriendlyUnitInfo

import scala.collection.mutable

class SquadBatch {

  val squads = new mutable.ArrayBuffer[Squad]
  val freelancers = new mutable.ArrayBuffer[FriendlyUnitInfo]

  private var started: Boolean = false
  private var finished: Boolean = false

  def processingStarted: Boolean = started
  def processingFinished: Boolean = finished

  def step(): Unit = {
    started = true
  }
}
