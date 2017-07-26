package Micro.Actions

import Micro.Actions.Basic._
import Micro.Actions.Combat.Decisionmaking.Fight
import Micro.Actions.Combat.Tactics.Tickle
import Micro.Actions.Combat.Maneuvering.Duck
import Micro.Actions.Commands.{Attack, Travel}
import Micro.Actions.Protoss.Meld
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Idle extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = {
    true
  }
  
  override def perform(unit: FriendlyUnitInfo) {
  
    ////////////
    // Intent //
    ////////////
    
    unit.action.toReturn        = unit.action.intent.toReturn
    unit.action.toTravel        = unit.action.intent.toTravel
    unit.action.toAttack        = unit.action.intent.toAttack
    unit.action.toGather        = unit.action.intent.toGather
    unit.action.toBuild         = unit.action.intent.toBuild
    unit.action.toBuildTile     = unit.action.intent.toBuildTile
    unit.action.toTrain         = unit.action.intent.toTrain
    unit.action.toTech          = unit.action.intent.toTech
    unit.action.toUpgrade       = unit.action.intent.toUpgrade
    unit.action.toForm          = unit.action.intent.toForm
    unit.action.canFight        = unit.action.intent.canAttack
    unit.action.canFlee         = unit.action.intent.canFlee
    unit.action.canPursue       = unit.action.intent.canPursue
    unit.action.canCower        = unit.action.intent.canCower
    unit.action.canMeld         = unit.action.intent.canMeld
  
    /////////////////
    // Diagnostics //
    /////////////////
  
    unit.action.desireTeam        = 0.0
    unit.action.desireIndividual  = 0.0
    unit.action.desireTotal       = 0.0
  
    /////////
    // ACT //
    /////////
    
    actions.foreach(_.consider(unit))
    
    unit.action.shovers.clear()
  }
  
  private val actions = Vector(
    Cancel, //Probably not actually used yet because candidates won't be in the Executor queue
    Meld,
    Tickle,
    Duck,
    Gather,
    Build,
    Unstick,
    Produce,
    Rally,
    ReloadInterceptors,
    ReloadScarabs,
    Fight,
    Attack,
    Travel
  )
}
