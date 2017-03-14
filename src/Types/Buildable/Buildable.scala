package Types.Buildable

import bwapi.{TechType, UnitType, UpgradeType}

abstract class Buildable {
  
  def unitOption      : Option[UnitType]    = None
  def unitsProduced   : Int                 = 0
  def techOption      : Option[TechType]    = None
  def upgradeOption   : Option[UpgradeType] = None
  def upgradeLevel    : Int                 = 0
  def minerals        : Int = 0
  def gas             : Int = 0
  def frames          : Int = 0
  def supplyRequired  : Int = 0
  def supplyProvided  : Int = 0
  
  def requirements:Iterable[Buildable] = List.empty
  def buildersOccupied:Iterable[BuildableUnit] = List.empty
  def buildersBorrowed:Iterable[BuildableUnit] = buildersOccupied.filterNot (b => consumesBuilders.contains(b.unit))
  def buildersConsumed:Iterable[BuildableUnit] = buildersOccupied.filter    (b => consumesBuilders.contains(b.unit))
  
  //Greater Spire is omitted as a hack since the Spire is still useful in the interim
  private val consumesBuilders = Set(
    UnitType.Protoss_Archon,
    UnitType.Protoss_Dark_Archon,
    UnitType.Zerg_Drone,
    UnitType.Zerg_Zergling,
    UnitType.Zerg_Hydralisk,
    UnitType.Zerg_Mutalisk,
    UnitType.Zerg_Ultralisk,
    UnitType.Zerg_Queen,
    UnitType.Zerg_Defiler,
    UnitType.Zerg_Guardian,
    UnitType.Zerg_Devourer,
    UnitType.Zerg_Hatchery,
    UnitType.Zerg_Lair,
    UnitType.Zerg_Hive,
    UnitType.Zerg_Extractor,
    UnitType.Zerg_Spawning_Pool,
    UnitType.Zerg_Hydralisk_Den,
    UnitType.Zerg_Evolution_Chamber,
    UnitType.Zerg_Queens_Nest,
    UnitType.Zerg_Ultralisk_Cavern,
    UnitType.Zerg_Defiler_Mound,
    UnitType.Zerg_Spire,
    UnitType.Zerg_Creep_Colony,
    UnitType.Zerg_Sunken_Colony,
    UnitType.Zerg_Spore_Colony
  )
}
