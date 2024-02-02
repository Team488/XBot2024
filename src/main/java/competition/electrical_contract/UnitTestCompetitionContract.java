package competition.electrical_contract;


import javax.inject.Inject;

public class UnitTestCompetitionContract extends CompetitionContract {

    @Inject
    public UnitTestCompetitionContract() {}

    @Override
    public boolean isDriveReady() {
        return true;
    }

    @Override
    public boolean isArmReady() {
        return true;
    }

    @Override
    public boolean isCollectorReady() {
        return true;
    }

    @Override
    public boolean isShooterReady() {
        return true;
    }
}