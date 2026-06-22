package com.etema.ragnarmmo.items.equipment;

public final class RagnarEquipmentSlots {
    public static final int MID_HEAD = 0;
    public static final int ACCESSORY_1 = 1;
    public static final int ACCESSORY_2 = 2;
    public static final int SLOT_COUNT = 3;

    private RagnarEquipmentSlots() {
    }

    public static RagnarEquipmentSlotType typeForIndex(int slot) {
        return switch (slot) {
            case MID_HEAD -> RagnarEquipmentSlotType.MID_HEAD;
            case ACCESSORY_1 -> RagnarEquipmentSlotType.ACCESSORY_1;
            case ACCESSORY_2 -> RagnarEquipmentSlotType.ACCESSORY_2;
            default -> throw new IllegalArgumentException("Unknown Ragnar equipment slot: " + slot);
        };
    }
}
