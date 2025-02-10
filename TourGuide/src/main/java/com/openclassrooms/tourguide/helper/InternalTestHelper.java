package com.openclassrooms.tourguide.helper;

/**
 * Helper class for managing internal test users.
 * This class is used to configure the number of test users for simulations and testing purposes.
 */
public class InternalTestHelper {

    /**
     * The default number of internal test users.
     * This value can be adjusted to scale testing up to 100,000 users.
     */
    private static int internalUserNumber = 100;

    /**
     * Sets the number of internal test users.
     *
     * @param internalUserNumber The number of users to be used in internal testing.
     */
    public static void setInternalUserNumber(int internalUserNumber) {
        InternalTestHelper.internalUserNumber = internalUserNumber;
    }

    /**
     * Gets the number of internal test users.
     *
     * @return The current number of internal test users.
     */
    public static int getInternalUserNumber() {
        return internalUserNumber;
    }
}
