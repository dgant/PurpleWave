package ProxyBwapi.Races

import Utilities.UnitFilters.IsAny
import ProxyBwapi.Techs.Techs
import ProxyBwapi.UnitClasses.UnitClasses
import ProxyBwapi.Upgrades.Upgrades
import bwapi.{TechType, UnitType, UpgradeType}

object Zerg {
  lazy val Drone                  = UnitClasses.get(UnitType.Zerg_Drone)
  lazy val Larva                  = UnitClasses.get(UnitType.Zerg_Larva)
  lazy val Egg                    = UnitClasses.get(UnitType.Zerg_Egg)
  lazy val Overlord               = UnitClasses.get(UnitType.Zerg_Overlord)
  lazy val Zergling               = UnitClasses.get(UnitType.Zerg_Zergling)
  lazy val Hydralisk              = UnitClasses.get(UnitType.Zerg_Hydralisk)
  lazy val Mutalisk               = UnitClasses.get(UnitType.Zerg_Mutalisk)
  lazy val Ultralisk              = UnitClasses.get(UnitType.Zerg_Ultralisk)
  lazy val Scourge                = UnitClasses.get(UnitType.Zerg_Scourge)
  lazy val Cocoon                 = UnitClasses.get(UnitType.Zerg_Cocoon)
  lazy val Guardian               = UnitClasses.get(UnitType.Zerg_Guardian)
  lazy val Devourer               = UnitClasses.get(UnitType.Zerg_Devourer)
  lazy val Queen                  = UnitClasses.get(UnitType.Zerg_Queen)
  lazy val Defiler                = UnitClasses.get(UnitType.Zerg_Defiler)
  lazy val LurkerEgg              = UnitClasses.get(UnitType.Zerg_Lurker_Egg)
  lazy val Lurker                 = UnitClasses.get(UnitType.Zerg_Lurker)
  lazy val Broodling              = UnitClasses.get(UnitType.Zerg_Broodling)
  lazy val InfestedTerran         = UnitClasses.get(UnitType.Zerg_Infested_Terran)
  lazy val Hatchery               = UnitClasses.get(UnitType.Zerg_Hatchery)
  lazy val Extractor              = UnitClasses.get(UnitType.Zerg_Extractor)
  lazy val SpawningPool           = UnitClasses.get(UnitType.Zerg_Spawning_Pool)
  lazy val HydraliskDen           = UnitClasses.get(UnitType.Zerg_Hydralisk_Den)
  lazy val EvolutionChamber       = UnitClasses.get(UnitType.Zerg_Evolution_Chamber)
  lazy val CreepColony            = UnitClasses.get(UnitType.Zerg_Creep_Colony)
  lazy val SporeColony            = UnitClasses.get(UnitType.Zerg_Spore_Colony)
  lazy val SunkenColony           = UnitClasses.get(UnitType.Zerg_Sunken_Colony)
  lazy val Lair                   = UnitClasses.get(UnitType.Zerg_Lair)
  lazy val QueensNest             = UnitClasses.get(UnitType.Zerg_Queens_Nest)
  lazy val Spire                  = UnitClasses.get(UnitType.Zerg_Spire)
  lazy val Hive                   = UnitClasses.get(UnitType.Zerg_Hive)
  lazy val GreaterSpire           = UnitClasses.get(UnitType.Zerg_Greater_Spire)
  lazy val UltraliskCavern        = UnitClasses.get(UnitType.Zerg_Ultralisk_Cavern)
  lazy val DefilerMound           = UnitClasses.get(UnitType.Zerg_Defiler_Mound)
  lazy val NydusCanal             = UnitClasses.get(UnitType.Zerg_Nydus_Canal)
  lazy val InfestedCommandCenter  = UnitClasses.get(UnitType.Zerg_Infested_Command_Center)
  lazy val SpelLDarkSwarm         = UnitClasses.get(UnitType.Spell_Dark_Swarm)
  lazy val SpireOrGreaterSpire    = IsAny(Spire, GreaterSpire)
  lazy val HatcheryLairOrHive     = IsAny(Hatchery, Lair, Hive)
  lazy val LairOrHive             = IsAny(Lair, Hive)
  lazy val ZerglingAttackSpeed    = Upgrades.get(UpgradeType.Adrenal_Glands)
  lazy val UltraliskSpeed         = Upgrades.get(UpgradeType.Anabolic_Synthesis)
  lazy val OverlordVisionRange    = Upgrades.get(UpgradeType.Antennae)
  lazy val UltraliskArmor         = Upgrades.get(UpgradeType.Chitinous_Plating)
  lazy val QueenEnergy            = Upgrades.get(UpgradeType.Gamete_Meiosis)
  lazy val HydraliskRange         = Upgrades.get(UpgradeType.Grooved_Spines)
  lazy val ZerglingSpeed          = Upgrades.get(UpgradeType.Metabolic_Boost)
  lazy val DefilerEnergy          = Upgrades.get(UpgradeType.Metasynaptic_Node)
  lazy val HydraliskSpeed         = Upgrades.get(UpgradeType.Muscular_Augments)
  lazy val OverlordSpeed          = Upgrades.get(UpgradeType.Pneumatized_Carapace)
  lazy val OverlordDrops          = Upgrades.get(UpgradeType.Ventral_Sacs)
  lazy val GroundArmor            = Upgrades.get(UpgradeType.Zerg_Carapace)
  lazy val GroundRangeDamage      = Upgrades.get(UpgradeType.Zerg_Missile_Attacks)
  lazy val GroundMeleeDamage      = Upgrades.get(UpgradeType.Zerg_Melee_Attacks)
  lazy val AirDamage              = Upgrades.get(UpgradeType.Zerg_Flyer_Attacks)
  lazy val AirArmor               = Upgrades.get(UpgradeType.Zerg_Flyer_Carapace)
  lazy val Burrow                 = Techs.get(TechType.Burrowing)
  lazy val Consume                = Techs.get(TechType.Consume)
  lazy val DarkSwarm              = Techs.get(TechType.Dark_Swarm)
  lazy val Ensnare                = Techs.get(TechType.Ensnare)
  lazy val InfestCommandCenter    = Techs.get(TechType.Infestation)
  lazy val LurkerMorph            = Techs.get(TechType.Lurker_Aspect)
  lazy val Parasite               = Techs.get(TechType.Parasite)
  lazy val Plague                 = Techs.get(TechType.Plague)
  lazy val SpawnBroodlings        = Techs.get(TechType.Spawn_Broodlings)
}
