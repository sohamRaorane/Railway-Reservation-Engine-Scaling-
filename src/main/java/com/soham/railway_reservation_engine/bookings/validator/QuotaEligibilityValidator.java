
package com.soham.railway_reservation_engine.bookings.validator;


import com.soham.railway_reservation_engine.bookings.dto.PassengerRequest;
import com.soham.railway_reservation_engine.common.enums.Gender;
import com.soham.railway_reservation_engine.passenger.entity.Passenger;
import com.soham.railway_reservation_engine.quota.entity.Quota;
import com.soham.railway_reservation_engine.user.entity.User;
import org.springframework.stereotype.Component;

//Following the srp
@Component
public class QuotaEligibilityValidator {
    public  void validate(PassengerRequest passenger , User user, Quota quota){
        String quotaCode = quota.getCode();
        switch (quota.getCode()) {

            case "GN":
                return;

            case "LD":
                validateLadiesQuota(passenger);
                return;

            case "SS":
                validateSeniorCitizenQuota(passenger);
                return;

            case "DEF":
                validateDefenceQuota(user);
                return;

            case "TQ":
                return;

            default:
                throw new RuntimeException(
                        "Invalid quota code : " + quota.getCode()
                );
        }
    }

    private void validateDefenceQuota(User user) {
        //BOOLEAN.TRUE  --> already handles the null case, so we don't need to check for null explicitly
        if (Boolean.TRUE.equals(user.getDefencePersonnel())) {
            return;
        }

        throw new RuntimeException(
                "User is not eligible for Defence Quota."
        );
    }

    private void validateLadiesQuota(PassengerRequest passenger) {
        if(passenger.getGender() != null && passenger.getGender().name().equals("FEMALE")){
            return;
        }else {
            throw new RuntimeException(
                    "Passenger is not eligible for Ladies Quota"
            );
        }
    }

    private void validateSeniorCitizenQuota(PassengerRequest passenger) {

        int minimumAge = (passenger.getGender() == Gender.MALE) ? 60 : 58;

        if (passenger.getAge() < minimumAge) {
            throw new RuntimeException(
                    "Passenger is not eligible for Senior Citizen quota."
            );
        }
    }


}


