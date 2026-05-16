package com.jotechhub.config;

import com.jotechhub.auth.AuthAccount;
import com.jotechhub.auth.AuthAccountRepository;
import com.jotechhub.auth.AuthProvider;
import com.jotechhub.category.Category;
import com.jotechhub.category.CategoryRepository;
import com.jotechhub.city.City;
import com.jotechhub.city.CityRepository;
import com.jotechhub.role.RoleType;
import com.jotechhub.tag.Tag;
import com.jotechhub.tag.TagRepository;
import com.jotechhub.university.University;
import com.jotechhub.university.UniversityRepository;
import com.jotechhub.user.User;
import com.jotechhub.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Configuration
@RequiredArgsConstructor
public class DataSeeder {

    private final UserRepository userRepository;
    private final AuthAccountRepository authAccountRepository;
    private final CityRepository cityRepository;
    private final UniversityRepository universityRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner seedData() {
        return args -> {
            seedAdmin();
            seedCities();
            seedUniversities();
            seedCategories();
            seedTags();
        };
    }

    private void seedAdmin() {
        String adminEmail = "admin@jotechhub.com";
        String adminRawPassword = "Admin@12345";

        Optional<User> existingAdmin = userRepository.findByEmail(adminEmail);

        User adminUser;
        if (existingAdmin.isEmpty()) {
            adminUser = User.builder()
                    .email(adminEmail)
                    .password(passwordEncoder.encode(adminRawPassword))
                    .role(RoleType.ADMIN)
                    .active(true)
                    .emailVerified(true)
                    .termsAccepted(true)
                    .termsAcceptedAt(LocalDateTime.now())
                    .build();

            adminUser = userRepository.save(adminUser);
        } else {
            adminUser = existingAdmin.get();
        }

        if (authAccountRepository.findByUserIdAndProvider(adminUser.getId(), AuthProvider.LOCAL).isEmpty()) {
            AuthAccount authAccount = AuthAccount.builder()
                    .user(adminUser)
                    .provider(AuthProvider.LOCAL)
                    .providerUserId(adminEmail)
                    .providerEmail(adminEmail)
                    .build();

            authAccountRepository.save(authAccount);
        }
    }

    private void seedCities() {
        // Count The Cities Of Jordan = 12 Cities
        List<String> cities = List.of(
                "Amman",
                "Irbid",
                "Zarqa",
                "Aqaba",
                "Al-Balqa",
                "Madaba",
                "Karak",
                "Mafraq",
                "Jerash",
                "Ajloun",
                "Ma'an",
                "Tafilah"
        );

        for (String cityName : cities) {
            if (!cityRepository.existsByNameIgnoreCase(cityName)) {
                cityRepository.save(
                        City.builder()
                                .name(cityName)
                                .active(true)
                                .build()
                );
            }
        }
    }

    private void seedUniversities() {
        // Count The University Private & Government = 28 University
        List<String> universities = List.of(
                "The University of Jordan",
                "University of Jordan - Aqaba Branch",
                "Yarmouk University",
                "Hashemite University",
                "Jordan University of Science and Technology",
                "The Hashemite University",
                "Al al-Bayt University",
                "Al-Balqa Applied University - Salt (Main Campus)",
                "Al-Balqa Applied University - Amman University College for Applied Professions",
                "Al-Balqa Applied University - Huson University College",
                "Al-Balqa Applied University - Karak University College",
                "Al-Balqa Applied University - Shobak University College",
                "Al-Balqa Applied University - Aqaba University College",
                "Al-Balqa Applied University - Irbid University College",
                "Al-Balqa Applied University - Ajloun University College",
                "Al-Balqa Applied University - Princess Alia University College",
                "Al-Hussein Bin Talal University",
                "Tafila Technical University",
                "German Jordanian University",
                "Amman Arab University",
                "Middle East University",
                "Jadara University",
                "Al-Ahliyya Amman University",
                "Applied Science Private University",
                "Philadelphia University",
                "Isra University",
                "University of Petra",
                "Al-Zaytoonah University of Jordan",
                "Jerash University",
                "Irbid National University",
                "Al-Zarqa University",
                "Princess Sumaya University for Technology",
                "American University of Madaba",
                "Ajloun National University",
                "Aqaba University of Technology",
                "Aqaba Medical Sciences University",
                "Ibn Sina University for Medical Sciences"
        );

        for (String universityName : universities) {
            if (!universityRepository.existsByNameIgnoreCase(universityName)) {
                universityRepository.save(
                        University.builder()
                                .name(universityName)
                                .active(true)
                                .build()
                );
            }
        }
    }

    private void seedCategories() {
        List<String> categories = List.of(
                "Training Course",
                "Bootcamp",
                "Workshop",
                "Hackathon",
                "Competition",
                "Conference"
        );

        for (String categoryName : categories) {
            if (!categoryRepository.existsByNameIgnoreCase(categoryName)) {
                categoryRepository.save(
                        Category.builder()
                                .name(categoryName)
                                .description(categoryName + " category")
                                .active(true)
                                .build()
                );
            }
        }
    }

    private void seedTags() {
        List<String> tags = List.of(
                "Software Engineering",
                "Backend Development",
                "Frontend Development",
                "Full Stack Development",
                "Java",
                "C++",
                "C",
                "React",
                "React Native",
                "Next Js",
                "Rust",
                "Tailwind",
                "Bootstrap",
                "Html",
                "CSS",
                "TypeScript",
                "Angular",
                "Python",
                "JavaScript",
                "Flutter",
                "Problem Solving",
                "Data Structure",
                "Algorithms",
                "Android Development",
                "Git",
                "GitHub",
                "GoLang",
                "iOS Development",
                "QA Testing",
                "Software Testing",
                "Automation Testing",
                "Database",
                "MySQL",
                "MongoDB",
                "Data Engineering",
                "Data Science",
                "Data Analysis",
                "Business Intelligence",
                "Power BI",
                "Machine Learning",
                "Deep Learning",
                "Generative AI",
                "Computer Vision",
                "NLP",
                "Cybersecurity",
                "Ethical Hacking",
                "Digital Forensics",
                "Cloud Computing",
                "AWS",
                "Azure",
                "Google Cloud",
                "DevOps",
                "Docker",
                "Kubernetes",
                "Linux",
                "Networking",
                "CCNA",
                "IoT",
                "Blockchain",
                "FinTech",
                "E-commerce",
                "Digital Transformation",
                "Product Management",
                "Project Management",
                "Agile",
                "Scrum",
                "UI/UX",
                "Game Development",
                "AR/VR",
                "Tech Entrepreneurship",
                "Startups",
                "Freelancing",
                "Remote Work",
                "Career Development"
        );

        for (String tagName : tags) {
            if (!tagRepository.existsByNameIgnoreCase(tagName)) {
                tagRepository.save(
                        Tag.builder()
                                .name(tagName)
                                .build()
                );
            }
        }
    }
}