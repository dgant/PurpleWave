package Performance

import java.io.File
import java.util.jar.JarFile

object ClassPreloading {
  // Application packages start with capitals. Include the Scala runtime too,
  // since rare collection-view classes can otherwise load during combat.
  private def selected(name: String): Boolean = (
    name.endsWith(".class")
    && ! name.startsWith("META-INF/")
    && (name.startsWith("scala/") || (name.charAt(0) >= 'A' && name.charAt(0) <= 'Z')))

  def preloadArchive(file: File, loader: ClassLoader): Int = {
    val archive = new JarFile(file)
    try {
      val entries = archive.entries()
      var loaded = 0
      while (entries.hasMoreElements) {
        val name = entries.nextElement().getName
        if (selected(name)) {
          // Load/link only: initializing modules here could change game state.
          Class.forName(name.substring(0, name.length - 6).replace('/', '.'), false, loader)
          loaded += 1
        }
      }
      loaded
    } finally archive.close()
  }

  def apply(): String = {
    val location = new File(getClass.getProtectionDomain.getCodeSource.getLocation.toURI)
    if ( ! location.isFile) return "Class preloading skipped outside a packaged JAR"
    val started = System.nanoTime
    val loaded = preloadArchive(location, getClass.getClassLoader)
    s"Preloaded $loaded application/Scala classes without initialization in ${(System.nanoTime - started) / 1000000}ms"
  }
}
