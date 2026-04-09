package ca.hanson.shiftflow_backend;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

@SpringBootTest
@ActiveProfiles("test")
class ApiIntegrationTests {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @Autowired
    private WebApplicationContext context;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void adminUsersAreRestricted() throws Exception {
        String adminToken = login("admin@shiftflow.com", "admin");
        String managerEmail = uniqueEmail("manager");
        String employeeEmail = uniqueEmail("employee");

        Long managerId = createUser(adminToken, managerEmail, "manager123", "MANAGER");
        Long employeeId = createUser(adminToken, employeeEmail, "employee123", "EMPLOYEE");

        mockMvc.perform(get("/api/admin/users")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        String managerToken = login(managerEmail, "manager123");

        mockMvc.perform(get("/api/admin/users")
                .header("Authorization", "Bearer " + managerToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete("/api/admin/users/" + employeeId)
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(delete("/api/admin/users/" + managerId)
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());
    }

    @Test
    void shiftAccessIsManagerOnlyExceptMe() throws Exception {
        String adminToken = login("admin@shiftflow.com", "admin");
        String managerEmail = uniqueEmail("manager");
        String employeeEmail = uniqueEmail("employee");

        createUser(adminToken, managerEmail, "manager123", "MANAGER");
        Long employeeId = createUser(adminToken, employeeEmail, "employee123", "EMPLOYEE");

        String managerToken = login(managerEmail, "manager123");
        String employeeToken = login(employeeEmail, "employee123");

        String createShiftBody = "{\n" +
                "  \"startTime\": \"2026-04-08T09:00:00\",\n" +
                "  \"endTime\": \"2026-04-08T17:00:00\",\n" +
                "  \"position\": \"Cashier\",\n" +
                "  \"location\": \"Store A\",\n" +
                "  \"assignedEmployeeId\": " + employeeId + "\n" +
                "}";

        String createShiftResponse = mockMvc.perform(post("/api/shifts")
                .header("Authorization", "Bearer " + managerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(createShiftBody))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode shiftJson = objectMapper.readTree(createShiftResponse);
        Long shiftId = shiftJson.get("id").asLong();

        mockMvc.perform(post("/api/shifts")
                .header("Authorization", "Bearer " + employeeToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(createShiftBody))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/shifts/all")
                .header("Authorization", "Bearer " + managerToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/shifts/employee/" + employeeId)
                .header("Authorization", "Bearer " + managerToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/shifts/me")
                .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/shifts/all")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isForbidden());

        String updateBody = "{\n  \"position\": \"Front Desk\"\n}";

        mockMvc.perform(put("/api/shifts/" + shiftId)
                .header("Authorization", "Bearer " + managerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateBody))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/api/shifts/" + shiftId)
                .header("Authorization", "Bearer " + managerToken))
                .andExpect(status().isNoContent());
    }

    @Test
    void weeklyAvailabilityFlow() throws Exception {
        String adminToken = login("admin@shiftflow.com", "admin");
        String managerEmail = uniqueEmail("manager");
        String employeeEmail = uniqueEmail("employee");

        createUser(adminToken, managerEmail, "manager123", "MANAGER");
        Long employeeId = createUser(adminToken, employeeEmail, "employee123", "EMPLOYEE");

        String managerToken = login(managerEmail, "manager123");
        String employeeToken = login(employeeEmail, "employee123");

        String createAvailabilityBody = "{\n" +
                "  \"dayOfWeek\": \"MONDAY\",\n" +
                "  \"startTime\": \"14:00:00\",\n" +
                "  \"endTime\": \"22:00:00\"\n" +
                "}";

        String availabilityResponse = mockMvc.perform(post("/api/availability")
                .header("Authorization", "Bearer " + employeeToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(createAvailabilityBody))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode availabilityJson = objectMapper.readTree(availabilityResponse);
        Long availabilityId = availabilityJson.get("id").asLong();

        mockMvc.perform(get("/api/availability/me")
                .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/availability/employee/" + employeeId)
                .header("Authorization", "Bearer " + managerToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/availability/employee/" + employeeId)
                .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isForbidden());

        String updateAvailabilityBody = "{\n" +
                "  \"dayOfWeek\": \"MONDAY\",\n" +
                "  \"startTime\": \"10:00:00\",\n" +
                "  \"endTime\": \"12:00:00\"\n" +
                "}";

        mockMvc.perform(put("/api/availability/" + availabilityId)
                .header("Authorization", "Bearer " + employeeToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateAvailabilityBody))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/api/availability/" + availabilityId)
                .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isNoContent());
    }

    private String login(String email, String password) throws Exception {
        String body = "{\n  \"email\": \"" + email + "\",\n  \"password\": \"" + password + "\"\n}";
        String response = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode json = objectMapper.readTree(response);
        return json.get("token").asText();
    }

    private Long createUser(String adminToken, String email, String password, String role) throws Exception {
        String body = "{\n" +
                "  \"firstName\": \"Test\",\n" +
                "  \"lastName\": \"User\",\n" +
                "  \"email\": \"" + email + "\",\n" +
                "  \"password\": \"" + password + "\",\n" +
                "  \"role\": \"" + role + "\"\n" +
                "}";

        String response = mockMvc.perform(post("/api/admin/users")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode json = objectMapper.readTree(response);
        return json.get("id").asLong();
    }

    private String uniqueEmail(String prefix) {
        return prefix + "_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8) + "@shiftflow.com";
    }
}
