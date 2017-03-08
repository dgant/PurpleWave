package Global.Resources.Scheduling

import Startup.With
import Types.Buildable.Buildable
import Types.BwapiTypes.{TechTypes, UpgradeTypes}
import Types.UnitInfo.FriendlyUnitInfo
import bwapi.{Order, TechType, UnitType, UpgradeType}

import scala.collection.{breakOut, mutable}
import scala.collection.mutable.ListBuffer

class ScheduleSimulator {
  
  /*
  
  First draft:
  
  Build representation of current state
  Go through the queue
    If item is buildable, build it and update state
    If item is not buildable
      If item will eventually become buildable (how do we tell?) push it back
      If item needs unscheduled requirements (how do we tell?) insert the requirement
  
  ---
  
  Second draft:
  
  Need to understand how to infer timings and resource availability
  
  Build representation of current state (units, mineral/gas/supply, mineral/gas rate, time)
  
  For each item I in the queue
    If item has unmet requirements
      Then:
        Insert that requirement in the queue at I
        Restart at step I
      Else:
        If item is buildable now
          Then:
            Build it and update the state
          Else:
            If item will eventually be buildable:
              Identify when it will be buildable
              Identify additional resources available at that time (minerals/gas sure, but how supply?)
              Reserve resources equivalent to Max(0, (Resource needed) - (Additional resource acquired by that time))
        
  
  */
  
  //Implementation of second draft of algorithm
  def simulate {
    //Model the current macro state
    var state = buildSimulationState
    
    //Create a copy of the current build queue
    var queue = With.scheduler.queue.toArray
  
    //For each item I in the queue
    //
    var index = 0
    while (index < queue.size) {
      
      val next = queue(index)
      
      // If item is buildable now
      //
      if (isBuildableNow(state, next)) {
        // Then build it and update the state
        //
        state.startBuilding(state, next)
        index += 1
      }
      // If the item will eventually be buildable
      //
      else if (isBuildableEventually(state, next)) {
        // Identify when it will be buildable
        // Identify additional resources available at that time (minerals/gas sure, but how supply?)
        // Reserve resources equivalent to Max(0, (Resource needed) - (Additional resource acquired by that time))
        
        index += 1
      }
      // Otherwise, the item has unmet requirements and will thus never be buildable
      //
      else {
        // Then insert the unmet requirements ahead of it in the queue
        //
        // TODO
  
        // Evaluate step I again
      }
    }
  }
  
  def isBuildableNow(state:ScheduleSimulation, buildable:Buildable):Boolean = {
    hasMineralRequirements(state, buildable) &&
    hasGasRequirements(state, buildable) &&
    hasPrerequisites(state, buildable)
    //TODO: Account for prerequisite availability
  }
  
  def isBuildableEventually(state:ScheduleSimulation, buildable:Buildable):Boolean = {
    hasMineralRequirements(state, buildable) || mineralsPerFrame(state) > 0 &&
    hasGasRequirements(state, buildable) || gasPerFrame(state) > 0 &&
    hasPrerequisites(state, buildable)
  }
  
  def hasMineralRequirements(state:ScheduleSimulation, buildable:Buildable):Boolean = {
    buildable.minerals <= state.minerals
  }
  
  def hasGasRequirements(state:ScheduleSimulation, buildable:Buildable):Boolean = {
    buildable.minerals <= state.minerals
  }
  
  def hasPrerequisites(state:ScheduleSimulation, buildable:Buildable):Boolean = {
    buildable.supplyRequired <= state.supplyAvailable
  }
  
  def unmetPrerequisites(state:ScheduleSimulation, buildable: Buildable): Iterable[Buildable] = {
    var output = new ListBuffer[Buildable]
    val units = new mutable.HashMap[UnitType, Int]
    (buildable.prerequisites ++ buildable.buildersOccupied).foreach(buildable => {
      
    })
  }
  
  def mineralsPerFrame(state:ScheduleSimulation):Double = {
    1
  }
  
  def gasPerFrame(state:ScheduleSimulation):Double = {
    1
  }
  
  def insertAt(queue:Array[Buildable], insertion:Buildable, index:Int):Array[Buildable] = {
    queue.take(index) ++ List(insertion) ++ queue.drop(index)
  }
  
  def buildSimulationState:ScheduleSimulation = {
    val unitsOwned = unitCount(false)
    val unitsAvailable = unitCount(true)
    val techsOwned = TechTypes.all.filter(With.game.self.hasResearched).to[mutable.HashSet]
    val upgradesOwned = UpgradeTypes.all.map(upgrade => (upgrade, With.game.self.getUpgradeLevel(upgrade))).toMap
    val upgradesOwnedMutable: mutable.Map[UpgradeType, Int] = upgradesOwned.map(identity)(breakOut)
    new ScheduleSimulation(
      frame = With.game.getFrameCount,
      minerals = With.game.self.minerals,
      gas = With.game.self.gas,
      supplyAvailable = With.game.self.supplyTotal - With.game.self.supplyUsed,
      unitsOwned = unitsOwned,
      unitsAvailable = unitsAvailable,
      techsOwned = techsOwned,
      upgradeLevels = upgradesOwnedMutable)
  }
  
  def unitCount(requireAvailable:Boolean):collection.mutable.HashMap[UnitType, Int] = {
    collection.mutable.HashMap(
      With.units.ours
        .filter(unit => ! requireAvailable || isAvailable(unit))
        .groupBy(_.utype)
        .map(pair => (pair._1, pair._2.size)).toSeq: _*)
  }
  
  def isAvailable(unit:FriendlyUnitInfo):Boolean = {
    unit.complete && ! macroOrders.contains(unit.order)
  }
  
 
  
  
  val macroOrders = Set(
    Order.ArchonWarp,
    Order.BuildAddon,
    Order.BuildingLiftOff,
    Order.BuildingLand,
    Order.BuildNydusExit,
    Order.CompletingArchonSummon,
    Order.ConstructingBuilding,
    Order.CreateProtossBuilding,
    Order.DarkArchonMeld,
    Order.IncompleteBuilding,
    Order.LiftingOff,
    Order.PlaceBuilding,
    Order.Train,
    Order.TrainFighter,
    Order.WarpIn,
    Order.ZergBirth,
    Order.ZergBuildingMorph,
    Order.ZergUnitMorph)
  
}
