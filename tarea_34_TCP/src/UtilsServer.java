import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class UtilsServer {

    public static String fileLister(String ruta) {
        StringBuilder sb = new StringBuilder();
        Path path = Paths.get(ruta);

        if (!Files.exists(path) || !Files.isDirectory(path)) {
            return "KO\n";
        }

        try (Stream<Path> stream = Files.list(path)) {
            sb.append("OK\n");
            stream.forEach(entry -> {
                String nombre = entry.getFileName().toString();
                long sizeKB = 0;
                try {
                    if (Files.isRegularFile(entry)) {
                        sizeKB = Files.size(entry) / 1024;
                    }

                    sb.append(nombre).append(" ").append(sizeKB).append("\n");
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
            sb.append("\n");
            return sb.toString();

        } catch (Exception e) {
            return "KO\n";
        }
    }

    public static String fileShow(String ruta) {
        StringBuilder sb = new StringBuilder();
        Path path = Paths.get(ruta);

        if (!Files.exists(path) || Files.isDirectory(path)) {
            return "KO\n";
        }

        try (Stream<String> lineas = Files.lines(path)) {
            long contador = lineas.count();
            sb.append("OK Lineas:" + contador + "\n");
            BufferedReader lector = new BufferedReader(new FileReader(path.toFile()));
            String leida = null;
            sb.append("Contenido: \n");
            while ((leida = lector.readLine()) != null) {
                sb.append(leida + "\n");
            }
            sb.append("\n");
            return sb.toString();
        } catch (Exception e) {
            return "KO\n";
        }
    }

    public static String fileDelete(String ruta) {
        Path path = Paths.get(ruta);

        try {
            if (!Files.exists(path)) return "KO\n";

            if (Files.isDirectory(path)) {
                try (Stream<Path> entries = Files.list(path)) {
                    if (entries.findFirst().isPresent()) {
                        return "KO\n";
                    }
                }
            }

            Files.delete(path);
            return "OK\n";

        } catch (IOException e) {
            return "KO\n";
        }

    }
}
