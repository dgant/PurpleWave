package Micro.Actions

import Micro.Actions.Basic._
import Micro.Actions.Combat.Decisionmaking.Fight
import Micro.Actions.Combat.Maneuvering.Duck
import Micro.Actions.Combat.Tactics.Tickle
import Micro.Actions.Commands.{Attack, Travel}
import Micro.Actions.Protoss.Meld
import Micro.Actions.Scouting.Scout
import Micro.Agency.MovementProfiles
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Idle extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = {
    true
  }
  
  override def perform(unit: FriendlyUnitInfo) {
  
    ///////////
    // Setup //
    ///////////
    
    unit.agent.toReturn        = unit.agent.intent.toReturn
    unit.agent.toTravel        = unit.agent.intent.toTravel
    unit.agent.toAttack        = unit.agent.intent.toAttack
    unit.agent.toGather        = unit.agent.intent.toGather
    unit.agent.toBuild         = unit.agent.intent.toBuild
    unit.agent.toBuildTile     = unit.agent.intent.toBuildTile
    unit.agent.toTrain         = unit.agent.intent.toTrain
    unit.agent.toTech          = unit.agent.intent.toTech
    unit.agent.toUpgrade       = unit.agent.intent.toUpgrade
    unit.agent.toForm          = unit.agent.intent.toForm
    unit.agent.canFight        = unit.agent.intent.canAttack
    unit.agent.canFlee         = unit.agent.intent.canFlee
    unit.agent.canPursue       = unit.agent.intent.canPursue
    unit.agent.canCower        = unit.agent.intent.canCower
    unit.agent.canMeld         = unit.agent.intent.canMeld
    unit.agent.canScout        = unit.agent.intent.canScout
  
    unit.agent.movementProfile = MovementProfiles.default
    
    /////////////////
    // Diagnostics //
    /////////////////
  
    unit.agent.desireTeam        = 1.0
    unit.agent.desireIndividual  = 1.0
    unit.agent.desireTotal       = 1.0
  
    /////////
    // ACT //
    /////////
    
    actions.foreach(_.consider(unit))
    
    unit.agent.shovers.clear()
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
    Pardon,
    Scout,
    Fight,
    Attack,
    Travel
  )
}
