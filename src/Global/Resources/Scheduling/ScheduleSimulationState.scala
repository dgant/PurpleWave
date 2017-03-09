package Global.Resources.Scheduling

import Types.Buildable.Buildable
import bwapi.{TechType, UnitType, UpgradeType}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

class ScheduleSimulationState(
  var frame               : Int,
  var minerals            : Int,
  var gas                 : Int,
  var supplyAvailable     : Int,
  var unitsOwned          : mutable.HashMap[UnitType, Int],
  var unitsAvailable      : mutable.HashMap[UnitType, Int],
  var techsOwned          : mutable.Set[TechType],
  var upgradeLevels       : mutable.Map[UpgradeType, Int]) {
  
  val futureEvents = new ScheduleSimulationEventQueue
  
  ////////////////////////////////////////
  // Understanding the simulation state //
  ////////////////////////////////////////

  def isBuildableEventually(buildable:Buildable):Boolean = {
    (hasMineralRequirements(buildable) || mineralsPerFrame > 0) &&
    (hasGasRequirements(buildable)     || gasPerFrame      > 0) &&
    (hasPrerequisites(buildable)       || false) //TODO: See if prerequisites will be built
  }
  
  def projectStartFrame(buildable: Buildable):Int = {
    Math.max(framesBeforeMinerals(buildable.minerals), framesBeforeGas(buildable.gas))
  }
  
  def hasMineralRequirements(buildable:Buildable) : Boolean = buildable.minerals <= minerals
  def hasGasRequirements(buildable:Buildable)     : Boolean = buildable.gas <= gas
  
  def hasPrerequisites(buildable:Buildable):Boolean = {
    buildable.supplyRequired <= supplyAvailable &&
    unmetPrerequisites(buildable).isEmpty
  }
  
  def prerequisites(buildable: Buildable):Iterable[Buildable] = {
    buildable.requirements ++ buildable.buildersOccupied
  }
  
  def unmetPrerequisites(buildable: Buildable): Iterable[Buildable] = {
    val output = new ListBuffer[Buildable]
    val units = new mutable.HashMap[UnitType, Int]
  
    prerequisites(buildable).foreach(buildable => {
      buildable.unitOption.foreach(unit => units.put(unit, 1 + units.getOrElse(unit, 0)))
      var unmet = false
      unmet ||= buildable.techOption    .exists(tech    => ! techsOwned.contains(tech))
      unmet ||= buildable.upgradeOption .exists(upgrade => upgradeLevels.getOrElse(upgrade, 0) < buildable.upgradeLevel)
      unmet ||= buildable.unitOption    .exists(unit    => units(unit) > unitsAvailable(unit))
      if (unmet) output.append(buildable)
    })
    
    output
  }
  
  def mineralsPerFrame  : Double  = 0.25 //TODO
  def gasPerFrame       : Double  = 0.25 //TODO
  
  //Note that this linear project doesn't account for any additional workers we gain in the meantime
  def framesBeforeMinerals(target:Int):Int  = Math.ceil((target - minerals) / mineralsPerFrame).toInt
  def framesBeforeGas(target:Int):Int       = Math.ceil((target - gas) / gasPerFrame).toInt
  
  ///////////////////////////////////
  // Mutating the simulation state //
  ///////////////////////////////////
  
  def stepForward() {
    if (futureEvents.isEmpty) throw new Exception("Attempting to run past end of simulation")
    val nextEvent = futureEvents.dequeue()
    val frameDifferential = nextEvent.frameEnd - frame
    minerals += (frameDifferential * mineralsPerFrame).toInt
    gas      += (frameDifferential * gasPerFrame).toInt
    finishBuilding(nextEvent.buildable, nextEvent.isImplicit)
    frame += frameDifferential
  }
  
  //Queue building at its projected start frame
  //Reserve only the resources necessary to reserve, based on when we expect to start the building
  def startBuilding(buildable:Buildable):SimulationEvent = {
    val frameStart  = projectStartFrame(buildable)
    reserveResources(buildable, frameStart)
    reserveBuilders(buildable, frameStart)
    enqueueBuildable(buildable, frameStart)
  }
  
  def reserveResources(buildable:Buildable, startFrame:Int) {
    val framesInFuture = startFrame - frame
    minerals -= Math.max(0, buildable.minerals - mineralsPerFrame * framesInFuture).toInt
    gas      -= Math.max(0, buildable.minerals - gasPerFrame * framesInFuture).toInt
    supplyAvailable -= buildable.supplyRequired
  }
  
  def reserveBuilders(buildable:Buildable, startFrame:Int) {
    //TODO: Don't add builder back if it's consumed
    //TODO: Try to account for travel time
    buildable.buildersOccupied.foreach(builder => {
      unitsAvailable(builder.unit) -= 1
      futureEvents.enqueue(new SimulationEvent(builder, startFrame, startFrame + buildable.frames, isImplicit = true))
    })
  }
  
  def enqueueBuildable(buildable: Buildable, startFrame:Int):SimulationEvent = {
    buildable.buildersConsumed.foreach(builder => unitsOwned(builder.unit) -= 1)
    val output = new SimulationEvent(buildable, startFrame, startFrame + buildable.frames)
    futureEvents.enqueue(output)
    output
  }
  
  def finishBuilding(buildable: Buildable, isImplicit: Boolean) {
    supplyAvailable += buildable.supplyProvided
    buildable.unitOption.filterNot(x => isImplicit).foreach(addOwnedUnit)
    buildable.unitOption.foreach(addAvailableUnit)
    buildable.techOption.foreach(addTech)
    buildable.upgradeOption.foreach(addUpgrade(_, buildable.upgradeLevel))
  }
  
  def addOwnedUnit(unitType: UnitType)                = unitsOwned.put(unitType, 1 + unitsOwned.getOrElse(unitType, 0))
  def addAvailableUnit(unitType: UnitType)            = unitsAvailable.put(unitType, 1 + unitsAvailable.getOrElse(unitType, 0))
  def addTech(techType: TechType)                     = techsOwned.add(techType)
  def addUpgrade(upgradeType: UpgradeType, level:Int) = upgradeLevels.put(upgradeType, level)
}