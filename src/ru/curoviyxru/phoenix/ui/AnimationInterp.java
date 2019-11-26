package ru.curoviyxru.phoenix.ui;

/**
 *
 * @author Roman Lahin
 */
public class AnimationInterp {

    public static final byte INTERP_LINEAR = 0, 
			INTERP_SQR = 1, 
			INTERP_INVSQR = 2, 
			INTERP_SIN = 3;
    public boolean started; //снаружи не менять! храню открыто, чтобы не вызывать лишний раз функцию
    private long startedAt;
    private int animationLength;
    public int startValue, endValue, value;
    public byte interpolationType;

    public AnimationInterp(int interpolationType) {
        this.interpolationType = (byte) interpolationType;
    }

    /**
     *
     * @return Возвращает сколько продлилась текущая анимация
     */
    public int getAnimationLasted() {
        if (!started) {
            return animationLength;
        }
        return (int) (System.currentTimeMillis() - startedAt);
    }

    /**
     * Запускает анимацию используя старые values
     *
     * @param animationLength длительность анимации
     */
    public void startContinue(int animationLength) {
        this.animationLength = animationLength;
        startedAt = System.currentTimeMillis();
        started = true;
        startValue = value;
    }

    public void start(int animationLength) {
        this.animationLength = animationLength;
        startedAt = System.currentTimeMillis();
        started = true;
        value = startValue;
    }

    public void end() {
        value = endValue;
        started = false;
    }

    public boolean update() {
        if (!started) {
            return false;
        }

        long currentStep = System.currentTimeMillis() - startedAt;

        if (currentStep >= animationLength) {
            end();
            return true;
        }

        switch (interpolationType) {
            case INTERP_SQR:
                currentStep = (currentStep * currentStep) / animationLength;
                break;
            case INTERP_INVSQR:
                currentStep = animationLength - currentStep;
                currentStep = (currentStep * currentStep) / animationLength;
                currentStep = animationLength - currentStep;
                break;
            case INTERP_SIN:
                currentStep = (int) AppCanvas.round(
						(0.5 - Math.cos(currentStep * Math.PI / animationLength) / 2)
						* animationLength);
                break;
            default:
                break;
        }

        long currentStep1 = animationLength - currentStep;

        value = (int) ((long) (startValue * currentStep1 + endValue * currentStep) / animationLength);

        return false;
    }
}
