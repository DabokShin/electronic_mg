package kst.ksti.chauffeur.model;

public class AllocationSelectModel {
    public long allocationIdx;      // 예약배차 ID
    public String ttsText;          // TTS 텍스트
    public boolean isAction;        // 예약배차 실행 되었는지?
    public long fcmRecvTime;        // 예약배차 푸시 받은 시간

    public long getAllocationIdx() {
        return allocationIdx;
    }

    public void setAllocationIdx(long allocationIdx) {
        this.allocationIdx = allocationIdx;
    }

    public boolean isAction() {
        return isAction;
    }

    public void setAction(boolean action) {
        isAction = action;
    }
}
