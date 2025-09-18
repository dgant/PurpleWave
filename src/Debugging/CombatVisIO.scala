package Debugging

import Information.Battles.Types.Battle
import Lifecycle.With

import java.io.{DataOutputStream, File, FileOutputStream, PrintWriter}
import java.nio.charset.StandardCharsets
import scala.collection.mutable.ArrayBuffer

object CombatVisIO {

  @volatile private var launched: Boolean = false

  private def baseDir: File = new File(With.bwapiData.write, "combatvis")
  private def mapsDir: File = new File(baseDir, "maps")
  private def simFile: File = new File(baseDir, "simulation.json")

  def ensureDirs(): Unit = {
    try {
      if (!baseDir.exists()) baseDir.mkdirs()
      if (!mapsDir.exists()) mapsDir.mkdirs()
    } catch { case exception: Exception => With.logger.quietlyOnException(exception) }
  }

  def exportMapData(): Unit = {
    try {
      ensureDirs()
      val hash = With.game.mapHash()
      val outFile = new File(mapsDir, hash + ".mapbin")
      if (outFile.exists()) return
      val dos = new DataOutputStream(new FileOutputStream(outFile))
      try {
        val tileW = With.mapTileWidth
        val tileH = With.mapTileHeight
        val walkW = With.mapWalkWidth
        val walkH = With.mapWalkHeight
        // Header
        dos.writeInt(tileW)
        dos.writeInt(tileH)
        dos.writeInt(walkW)
        dos.writeInt(walkH)
        // Ground heights per tile (byte per tile)
        var y = 0
        while (y < tileH) {
          var x = 0
          while (x < tileW) {
            val h = With.game.getGroundHeight(x, y) & 0xFF
            dos.writeByte(h)
            x += 1
          }
          y += 1
        }
        // Walkability per mini-tile (byte per walk tile)
        var wy = 0
        while (wy < walkH) {
          var wx = 0
          while (wx < walkW) {
            val w = if (With.game.isWalkable(wx, wy)) 1 else 0
            dos.writeByte(w)
            wx += 1
          }
          wy += 1
        }
      } finally {
        dos.close()
      }
    } catch { case exception: Exception => With.logger.quietlyOnException(exception) }
  }

  def prepareEmptySimFile(): Unit = {
    try {
      ensureDirs()
      val json = s"""{\n  \"mapHash\": \"${With.game.mapHash()}\",\n  \"updatedAt\": ${System.currentTimeMillis()},\n  \"units\": [],\n  \"frames\": []\n}\n"""
      val pw = new PrintWriter(simFile, StandardCharsets.UTF_8.name())
      try pw.print(json) finally pw.close()
    } catch { case exception: Exception => With.logger.quietlyOnException(exception) }
  }

  // framesLog: lines of format: frame|id,isFriendly,x,y,alive,width,height;
  def writeSimulationLog(battle: Battle, framesLog: ArrayBuffer[String]): Unit = {
    try {
      ensureDirs()
      val hash = With.game.mapHash()

      // Collect unit metadata (id -> (width,height, side)) from framesLog first frame
      val metas: Vector[(Int, Int, Int, Boolean)] = {
        val buf = new scala.collection.mutable.ArrayBuffer[(Int, Int, Int, Boolean)]()
        framesLog.headOption.foreach { line =>
          val parts = line.split("\\|")
          if (parts.length >= 2) {
            val units = parts(1).split(';')
            var i = 0
            while (i < units.length) {
              val u = units(i)
              if (u.nonEmpty) {
                val f = u.split(',')
                if (f.length >= 7) {
                  val id = try f(0).toInt catch { case _: Exception => -1 }
                  val friendly = f(1).toBoolean
                  val w = try f(5).toInt catch { case _: Exception => 0 }
                  val h = try f(6).toInt catch { case _: Exception => 0 }
                  if (id >= 0) buf += ((id, w, h, friendly))
                }
              }
              i += 1
            }
          }
        }
        buf.distinct.toVector
      }

      val sb = new StringBuilder(1024 * 128)
      sb.append('{')
      sb.append("\"mapHash\":\"").append(hash).append("\",")
      sb.append("\"updatedAt\":").append(System.currentTimeMillis()).append(',')
      // Units meta
      sb.append("\"units\":[")
      var first = true
      metas.foreach { case (id, w, h, friendly) =>
        if (!first) sb.append(',') else first = false
        sb.append('{')
        sb.append("\"id\":").append(id).append(',')
        sb.append("\"friendly\":").append(if (friendly) "true" else "false").append(',')
        sb.append("\"width\":").append(w).append(',')
        sb.append("\"height\":").append(h)
        sb.append('}')
      }
      sb.append("],")
      // Frames
      sb.append("\"frames\":[")
      var firstFrame = true
      framesLog.foreach { line =>
        // Each line is already compact; just wrap as a JSON string? Better: parse and rebuild safe JSON
        // We control the format; convert to a JSON-ish object
        // line: frame|id,isFriendly,x,y,alive,width,height;
        val parts = line.split("\\|")
        if (parts.length >= 2) {
          val fnum = parts(0)
          val unitsStr = parts(1)
          if (!firstFrame) sb.append(',') else firstFrame = false
          sb.append('{')
          sb.append("\"f\":").append(fnum).append(',')
          sb.append("\"u\":[")
          var firstUnit = true
          val units = unitsStr.split(';')
          var i = 0
          while (i < units.length) {
            val u = units(i)
            if (u.nonEmpty) {
              val fields = u.split(',')
              if (fields.length >= 7) {
                if (!firstUnit) sb.append(',') else firstUnit = false
                sb.append('{')
                sb.append("\"id\":").append(fields(0)).append(',')
                sb.append("\"friendly\":").append(fields(1)).append(',')
                sb.append("\"x\":").append(fields(2)).append(',')
                sb.append("\"y\":").append(fields(3)).append(',')
                sb.append("\"alive\":").append(fields(4)).append(',')
                sb.append("\"width\":").append(fields(5)).append(',')
                sb.append("\"height\":").append(fields(6))
                if (fields.length >= 11) {
                  sb.append(',')
                  sb.append("\"hp\":").append(fields(7)).append(',')
                  sb.append("\"sh\":").append(fields(8)).append(',')
                  sb.append("\"hpMax\":").append(fields(9)).append(',')
                  sb.append("\"shMax\":").append(fields(10))
                }
                if (fields.length >= 12) {
                  sb.append(',')
                  sb.append("\"tgt\":").append(fields(11))
                }
                if (fields.length >= 13) {
                  sb.append(',')
                  sb.append("\"fly\":").append(fields(12))
                }
                sb.append('}')
              }
            }
            i += 1
          }
          sb.append("]")
          // Optional events part
          if (parts.length >= 3) {
            val evStr = parts(2)
            var firstAttack = true
            var firstDeath = true
            // We'll buffer attacks and deaths separately
            val attacksSb = new StringBuilder()
            val deathsSb = new StringBuilder()
            val tokens = evStr.split(';')
            var ti = 0
            while (ti < tokens.length) {
              val t = tokens(ti)
              if (t.nonEmpty) {
                if (t.startsWith("a:")) {
                  val pv = t.substring(2)
                  val gt = pv.indexOf('>')
                  if (gt > 0) {
                    if (!firstAttack) attacksSb.append(',') else firstAttack = false
                    attacksSb.append('[').append(pv.substring(0, gt)).append(',').append(pv.substring(gt + 1)).append(']')
                  }
                } else if (t.startsWith("d:")) {
                  val id = t.substring(2)
                  if (!firstDeath) deathsSb.append(',') else firstDeath = false
                  deathsSb.append(id)
                }
              }
              ti += 1
            }
            if (attacksSb.nonEmpty) { sb.append(',').append("\"a\":[").append(attacksSb.toString()).append(']') }
            if (deathsSb.nonEmpty)  { sb.append(',').append("\"d\":[").append(deathsSb.toString()).append(']') }
          }
          sb.append('}')
        }
      }
      sb.append("]}")

      val pw = new PrintWriter(simFile, StandardCharsets.UTF_8.name())
      try pw.print(sb.toString()) finally pw.close()
    } catch { case exception: Exception => With.logger.quietlyOnException(exception) }
  }

  def launchVisualizer(): Unit = {
    try {
      ensureDirs()
      // Launch visualizer in a background thread within same JVM to keep it simple
      val path = simFile.getAbsolutePath
      val thread = new Thread(new Runnable { override def run(): Unit = {
        try Debugging.CombatVisualizer.main(Array(path)) catch { case exception: Exception => With.logger.quietlyOnException(exception) }
      }})
      thread.setName("CombatVisualizer")
      thread.setDaemon(true)
      thread.start()
      launched = true
    } catch { case exception: Exception => With.logger.quietlyOnException(exception) }
  }

  def terminateVisualizer(): Unit = {
    try {
      if (launched) {
        try Debugging.CombatVisualizer.requestClose() catch { case exception: Exception => With.logger.quietlyOnException(exception) }
      }
    } catch { case exception: Exception => With.logger.quietlyOnException(exception) } finally { launched = false }
  }
}
