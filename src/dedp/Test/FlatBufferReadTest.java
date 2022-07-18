package dedp.Test;
import java.io.FileInputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import com.google.flatbuffers.FlatBufferBuilder;
import dedp.DistanceOracles.FlatBuffer.Oracle;
import dedp.DistanceOracles.FlatBuffer.Wsp;

import java.nio.file.Files;

public class FlatBufferReadTest {
    public static void main(String args[]) throws Exception {
        //FlatBufferBuilder builder = new FlatBufferBuilder(900);
      /*  String filename = "./DOResult/0_24.out";
        byte[] bytes=null;
        try (FileInputStream fis = new FileInputStream(filename)) {
            bytes = fis.readAllBytes();

        } catch (Exception e) {
            e.printStackTrace();
        }
        java.nio.ByteBuffer buf = java.nio.ByteBuffer.wrap(bytes);
        Oracle oracle = Oracle.getRootAsOracle(buf);
        Wsp.Vector vector = oracle.wspsVector();
        for(int i=0; i<vector.length();i++){
            Wsp wsp = vector.get(i);
            System.out.println(wsp.code()+","+wsp.level()+","+wsp.distance());
        }*/
        dedp.DistanceOracles.EDP_DO_Test t = new dedp.DistanceOracles.EDP_DO_Test();
        t.loadGraph(300000);
        t.index.partitions[1].ConnectedComponents.getConnectedComponent(0).inputDOFlatBuffer();
    }

}
