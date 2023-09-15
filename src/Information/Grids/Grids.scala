
package Information.Grids

import Information.Grids.Construction._
import Information.Grids.Floody._
import Information.Grids.Movement._
import Information.Grids.Miscellaneous.{GridFormationSlots, GridPsionicStorm}
import Information.Grids.Versioned.{GridVersionedBoolean, GridVersionedDouble, GridVersionedInt}
import Information.Grids.Vision._
import Lifecycle.With
import Performance.Tasks.TimedTask
import Performance.Timer

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

class Grids extends TimedTask {
  skipsMax = 1
  alwaysSafe = true // Until proven otherwise

  private val _grids = new ArrayBuffer[Grid]()
  private val _unitGrids = new ArrayBuffer[GridUnits]()
  def all: Seq[Grid] = _grids
  def unit: Seq[GridUnits] = _unitGrids
  private def add[T <: Grid](grid: T): T = { _grids += grid; grid }
  private def add[T <: Grid](code: String, grid: T): T = {
    grid.code = code
    if (grid.isInstanceOf[GridUnits]) {
      _unitGrids += grid.asInstanceOf[GridUnits]
    }
    add(grid)
  }

  // Pass-through calls to other grids
  val buildable                   = add("b",    new GridBuildable)
  val walkable                    = add("w",    new GridWalkable)

  // Initialized once
  val buildableTerrain            = add("bt",   new GridBuildableTerrain)
  val buildableTownHall           = add("bh",   new GridBuildableTownHall)
  val buildable63                 = add("b4",   new GridBuildableWH(6, 3))
  val buildable43                 = add("b4",   new GridBuildableWH(4, 3))
  val buildable32                 = add("b3",   new GridBuildableWH(3, 2))
  val buildable22                 = add("b2",   new GridBuildableWH(2, 2))
  val walkableTerrain             = add("wt",   new GridWalkableTerrain)
  val mobilityTerrain             = add("mt",   new GridMobilityTerrain)
  val scoutingPathsBases          = add("spb",  new GridScoutingPathsBases)
  val scoutingPathsStartLocations = add("sps",  new GridScoutingPathsStartLocations)

  // Updated by tasks
  val lastSeen                    = add("ls",   new GridLastSeen)
  val psi2Height                  = add("p2",   new GridPsi2Height)
  val psi3Height                  = add("p3",   new GridPsi3Height)
  val psionicStorm                = add("ps",   new GridPsionicStorm)
  val unwalkableUnits             = add("wu",   new GridUnwalkableUnits)

  // Based on unit position
  val units                       = add("u",    new GridUnits)
  val enemyRangeAir               = add("era",  new GridEnemyRangeAir)
  val enemyRangeGround            = add("erg",  new GridEnemyRangeGround)
  val enemyRangeAirGround         = add("erag", new GridEnemyRangeAirGround)
  val enemyVulnerabilityGround    = add("evg",  new GridEnemyVulnerabilityGround)
  val enemyDetection              = add("ed",   new GridEnemyDetection)
  val enemyVision                 = add("ev",   new GridEnemyVision)
  val friendlyDetection           = add("fd",   new GridFriendlyDetection)

  // Other
  val formationSlots              = add("fs",   new GridFormationSlots)

  private var _selected: Option[Grid] = None
  def selected: Option[Grid] = _selected
  def parseCommand(code: String): Boolean = {
    _selected = all.find("g" + _.code == code).orElse(With.geography.zones.find("g" + _.name.toLowerCase == code.toLowerCase).map(_.distanceGrid))
    if (_selected.isDefined) {
      With.manners.chat(f"Selected ${_selected.get.getClass.getSimpleName.replace("Grid", "")}")
    } else if (code == "gm") {
      _selected = Some(With.geography.ourMain.zone.distanceGrid)
    } else if (code == "gn") {
      _selected = Some(With.geography.ourNatural.zone.distanceGrid)
    } else if (code == "ge") {
      _selected = Some(With.scouting.enemyHome.zone.distanceGrid)
    } else if (code == "g") {
      _selected = None
    }
    _selected.nonEmpty
  }

  private val _disposableBoolean  = new GridVersionedBoolean
  private val _disposableInt      = new GridVersionedInt
  private val _disposableDouble   = new GridVersionedDouble

  def disposableBoolean(): GridVersionedBoolean = {
    _disposableBoolean.update()
    _disposableBoolean
  }
  def disposableInt(): GridVersionedInt = {
    _disposableInt.update()
    _disposableInt
  }
  def disposableDouble(): GridVersionedDouble = {
    _disposableDouble.update()
    _disposableDouble
  }

  def buildableW(w: Int): GridBuildableWH = {
    w match {
      case 6 => buildable63
      case 4 => buildable43
      case 3 => buildable32
      case _ => buildable22
    }
  }

  private val _updateQueue = new mutable.Queue[Grid]()

  override protected def onRun(budgetMs: Long): Unit = {
    val timer = new Timer(budgetMs)
    var gridsUpdated = 0
    if (_updateQueue.isEmpty) { _updateQueue ++= all }
    do {
      val next = _updateQueue.dequeue()
      _updateQueue.enqueue(next)
      next.update()
      gridsUpdated += 1
    } while (gridsUpdated < all.size && timer.greenLight)
  }
}
