package Utilities.Enrichment

import bwapi.WalkPosition

case object EnrichPosition {
  implicit class EnrichedPosition(position: bwapi.Position) {
    def toWalkPosition:WalkPosition = {
      new WalkPosition(position.getX / 4, position.getY / 4)
    }
  }
  
  implicit class EnrichedTilePosition(position: bwapi.TilePosition) {
    def toWalkPosition:WalkPosition = {
      new WalkPosition(position.getX * 8, position.getY * 8)
    }
  }
}
