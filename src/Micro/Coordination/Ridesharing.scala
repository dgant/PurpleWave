package Micro.Coordination

import ProxyBwapi.UnitInfo.FriendlyUnitInfo

import scala.collection.mutable.ArrayBuffer

class Ridesharing {

  case class Claim(transport: FriendlyUnitInfo, passenger: FriendlyUnitInfo) {
    var age: Int = 0
  }

  var claims = new ArrayBuffer[Claim]
  def claim(transport: FriendlyUnitInfo, passenger: FriendlyUnitInfo): Unit = {
    claims += Claim(transport, passenger)
  }

  def onAgentCycle(): Unit = {
    claims.foreach(_.age += 1)
    claims --= claims.filter(_.age > 2)
  }
}
