package fr.mrqsdf.bbmodelreader;

import fr.mrqsdf.bbmodelreader.data.BbModel;
import fr.mrqsdf.bbmodelreader.data.Face;
import fr.mrqsdf.bbmodelreader.data.Outliner;
import fr.mrqsdf.bbmodelreader.data.OutlinerChild;
import fr.mrqsdf.bbmodelreader.data.OutlinerElementRef;
import fr.mrqsdf.bbmodelreader.data.OutlinerGroupRef;
import fr.mrqsdf.bbmodelreader.data.animation.Animation;
import fr.mrqsdf.bbmodelreader.data.animation.Keyframe;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class BbModelParsingTest {

    @Test
    void parsesOutlinerChildrenAndKeyframes() throws Exception {
        String json = "{"
                + "\"meta\":{\"format_version\":\"5.0\",\"model_format\":\"java_block\",\"box_uv\":false},"
                + "\"elements\":[{"
                + "  \"uuid\":\"el1\",\"type\":\"cube\","
                + "  \"from\":[0,0,0],\"to\":[1,1,1],"
                + "  \"faces\":{\"north\":{\"uv\":[0,0,1,1],\"texture\":0}}"
                + "}],"
                + "\"outliner\":[{"
                + "  \"uuid\":\"g1\",\"name\":\"root\",\"children\":[\"el1\",{"
                + "    \"uuid\":\"g2\",\"name\":\"child\",\"children\":[\"el1\"]"
                + "  }]"
                + "}],"
                + "\"animations\":[{"
                + "  \"uuid\":\"a1\",\"name\":\"test\",\"loop\":\"once\",\"length\":1.5,\"snapping\":20,"
                + "  \"animators\":{"
                + "    \"el1\":{"
                + "      \"name\":\"Cube\",\"type\":\"bone\","
                + "      \"keyframes\":[{"
                + "        \"channel\":\"rotation\",\"uuid\":\"k1\",\"time\":0,"
                + "        \"interpolation\":\"linear\","
                + "        \"data_points\":[{\"x\":\"0\",\"y\":\"0\",\"z\":\"0\"}]"
                + "      }]"
                + "    }"
                + "  }"
                + "}]"
                + "}";

        File temp = File.createTempFile("bbmodel", ".bbmodel");
        Files.write(temp.toPath(), json.getBytes(StandardCharsets.UTF_8));

        BbModel model = BbModelReader.loadModel(temp, "test");
        assertNotNull(model.getMeta());
        assertEquals("5.0", model.getMeta().getFormatVersion());

        assertNotNull(model.getElements());
        assertEquals(1, model.getElements().length);
        Face.FaceValue north = model.getElements()[0].getFaces().getNorth();
        assertNotNull(north);
        assertEquals(0, north.getTexture());

        Outliner root = model.getOutliner()[0];
        assertEquals("g1", root.getUuid());
        assertEquals(2, root.getChildren().size());
        OutlinerChild firstChild = root.getChildren().get(0);
        assertInstanceOf(OutlinerElementRef.class, firstChild);
        assertEquals("el1", ((OutlinerElementRef) firstChild).getUuid());
        OutlinerChild secondChild = root.getChildren().get(1);
        assertInstanceOf(OutlinerGroupRef.class, secondChild);
        assertEquals("g2", ((OutlinerGroupRef) secondChild).getGroup().getUuid());

        Animation animation = model.getAnimations()[0];
        assertEquals(1.5f, animation.getLength());
        Map<String, ?> animators = animation.getAnimators();
        assertTrue(animators.containsKey("el1"));
        Keyframe keyframe = animation.getAnimators().get("el1").getKeyframes()[0];
        assertEquals("rotation", keyframe.getChannel());
        assertEquals(1, keyframe.getDataPoints().size());
        assertEquals("0", keyframe.getDataPoints().get(0).getX());
    }
}

