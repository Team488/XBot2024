package competition.subsystems.vision;

import edu.wpi.first.util.struct.Struct;

import java.nio.ByteBuffer;

public class SimpleNoteStruct implements Struct<SimpleNote> {
    @Override
    public Class<SimpleNote> getTypeClass() {
        return SimpleNote.class;
    }

    @Override
    public String getTypeString() {
        return "struct:SimpleNote";
    }

    @Override
    public int getSize() {
        return kSizeDouble * 3;
    }

    @Override
    public String getSchema() {
        return "double area; double yaw; double pitch";
    }

    @Override
    public SimpleNote unpack(ByteBuffer byteBuffer) {
        var area = byteBuffer.getDouble();
        var yaw = byteBuffer.getDouble();
        var pitch = byteBuffer.getDouble();
        return new SimpleNote(area, yaw, pitch);
    }

    @Override
    public void pack(ByteBuffer byteBuffer, SimpleNote simpleNote) {
        byteBuffer.putDouble(simpleNote.getArea());
        byteBuffer.putDouble(simpleNote.getYaw());
        byteBuffer.putDouble(simpleNote.getPitch());
    }
}
