package io.jay.springsecuritysample;

import lombok.AllArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final ConfirmationTokenService confirmationTokenService;
    private final EmailSenderService emailSenderService;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email).orElseThrow(()
                -> new RuntimeException("User with email cannot be found: " + email));
        return user;
    }

    void signUpUser(User user) {
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        String encryptedPassword = bCryptPasswordEncoder.encode(user.getPassword());
        user.setPassword(encryptedPassword);
        User createdUser = userRepository.save(user);
        ConfirmationToken confirmationToken = new ConfirmationToken(createdUser);
        confirmationTokenService.saveConfirmationToken(confirmationToken);
        this.sendConfirmationMail(user.getEmail(), confirmationToken.getConfirmationToken());
    }

    void confirmUser(ConfirmationToken confirmationToken) {
        User user = confirmationToken.getUser();
        user.setEnabled(true);
        userRepository.save(user);
        confirmationTokenService.deleteConfirmationToken(confirmationToken.getId());
    }

    void sendConfirmationMail(String userMail, String token) {
        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        simpleMailMessage.setTo(userMail);
        simpleMailMessage.setSubject("Mail Confirmation Link");
        simpleMailMessage.setFrom("youremail@domain.com");
        simpleMailMessage.setText(
                "Thank you for registering. Please click on the below link to activate your account." +
                        "http://localhost:8080/sign-up/confirm?token="
                        + token);
        emailSenderService.sendEmail(simpleMailMessage);
    }

}
