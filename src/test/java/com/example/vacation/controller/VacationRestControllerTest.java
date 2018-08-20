package com.example.vacation.controller;

import com.example.vacation.dao.VacationRepository;
import com.example.vacation.model.Vacation;
import com.example.vacation.model.VacationStatus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.http.MockHttpOutputMessage;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.io.IOException;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class VacationRestControllerTest {
    private static final String VACATION_URI = "/vacation/";
    private MediaType contentType = new MediaType(MediaType.APPLICATION_JSON.getType(),
            MediaType.APPLICATION_JSON.getSubtype(),
            Charset.forName("utf8"));

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private VacationRepository repository;

    private HttpMessageConverter mappingJackson2HttpMessageConverter;

    private Vacation vacation;

    @Test
    public void readVacation() throws Exception {
        this.mockMvc.perform(get(VACATION_URI + vacation.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(contentType))
                .andExpect(jsonPath("$.id", is(this.vacation.getId().intValue())))
                .andExpect(jsonPath("$.name", is(this.vacation.getName())));
    }

    @Test
    public void readVacationNotFound() throws Exception {
        this.mockMvc.perform(get(VACATION_URI + 2)).andDo(print()).andExpect(status().isNotFound());
    }

    @Test
    public void readVacationWrongArgument() throws Exception {
        this.mockMvc.perform(get(VACATION_URI + "qwe")).andDo(print()).andExpect(status().is4xxClientError());
    }

    @Test
    public void addVacation() throws Exception {

        Vacation vacation2 = createVacation("11.02.2018", "20.02.2018", "Петров А.Б.");
        this.mockMvc.perform(post(VACATION_URI)
                .contentType(contentType)
                .content(json(vacation2)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(contentType))
                .andExpect(jsonPath("$.name", is(vacation2.getName())));
        List<Vacation> all = repository.findAll();
        assertEquals(2, all.size());
    }

    @Test
    public void addVacationWrongDates() throws Exception {

        Vacation vacation2 = createVacation("20.02.2018", "11.02.2018", "Петров А.Б.");
        this.mockMvc.perform(post(VACATION_URI)
                .contentType(contentType)
                .content(json(vacation2)))
                .andExpect(status().isNotAcceptable());
        List<Vacation> all = repository.findAll();
        assertEquals(1, all.size());
    }

    @Test
    public void addVacationOverlapDatesBefore() throws Exception {

        Vacation vacation2 = createVacation("01.01.2018", "11.02.2018", "Сидоров В.Г.");
        this.mockMvc.perform(post(VACATION_URI)
                .contentType(contentType)
                .content(json(vacation2)))
                .andExpect(status().isConflict());
        List<Vacation> all = repository.findAll();
        assertEquals(1, all.size());
    }

    @Test
    public void addVacationOverlapDatesOneDay() throws Exception {

        Vacation vacation2 = createVacation("10.02.2018", "10.02.2018", "Сидоров В.Г.");
        this.mockMvc.perform(post(VACATION_URI)
                .contentType(contentType)
                .content(json(vacation2)))
                .andExpect(status().isConflict());
        List<Vacation> all = repository.findAll();
        assertEquals(1, all.size());
    }

    @Test
    public void addVacationOverlapDatesAfter() throws Exception {

        Vacation vacation2 = createVacation("10.02.2018", "20.02.2018", "Сидоров В.Г.");
        this.mockMvc.perform(post(VACATION_URI)
                .contentType(contentType)
                .content(json(vacation2)))
                .andExpect(status().isConflict());
        List<Vacation> all = repository.findAll();
        assertEquals(1, all.size());
    }

    @Test
    public void addVacationOverlapDatesInRange() throws Exception {

        Vacation vacation2 = createVacation("03.02.2018", "04.02.2018", "Сидоров В.Г.");
        this.mockMvc.perform(post(VACATION_URI)
                .contentType(contentType)
                .content(json(vacation2)))
                .andExpect(status().isConflict());

        List<Vacation> all = repository.findAll();
        assertEquals(1, all.size());
    }

    @Test
    public void updateVacation() throws Exception {

        vacation.setStatus(VacationStatus.ACCEPTED);

        MockHttpServletRequestBuilder builder =
                MockMvcRequestBuilders.put(VACATION_URI + vacation.getId())
                        .contentType(contentType)
                        .content(json(vacation));

        this.mockMvc.perform(builder)
                .andExpect(status().isOk());

        List<Vacation> all = repository.findAll();
        assertEquals(1, all.size());
    }

    @Test
    public void updateVacationOverlapDates() throws Exception {

        Vacation vacation2 = createVacation("01.03.2018", "10.03.2018", "Сидоров В.Г.");
        repository.save(vacation2);
        vacation2.setStart(fromString("01.01.2018"));

        MockHttpServletRequestBuilder builder =
                MockMvcRequestBuilders.put(VACATION_URI + vacation2.getId())
                        .contentType(contentType)
                        .content(json(vacation2));

        this.mockMvc.perform(builder)
                .andExpect(status().isConflict());

        List<Vacation> all = repository.findAll();
        assertEquals(2, all.size());
    }

    @Test
    public void updateVacationOverlapDates2() throws Exception {

        this.repository.deleteAllInBatch();

        List<Vacation> all = repository.findAll();
        assertEquals(0, all.size());

        Vacation vacation1 = createVacation("10.02.2018", "13.02.2018", "Иванов А.Б.");

        this.mockMvc.perform(post(VACATION_URI)
                .contentType(contentType)
                .content(json(vacation1)))
                .andExpect(status().isOk());
        all = repository.findAll();
        assertEquals(1, all.size());

        Vacation vacation2 = createVacation("10.02.2018", "16.04.2018", "Петров В.Г.");

        this.mockMvc.perform(post(VACATION_URI)
                .contentType(contentType)
                .content(json(vacation2)))
                .andExpect(status().isConflict());
        all = repository.findAll();
        assertEquals(1, all.size());
    }

    @Test
    public void updateVacationNotFound() throws Exception {

        MockHttpServletRequestBuilder builder =
                MockMvcRequestBuilders.put(VACATION_URI + 2)
                        .contentType(contentType)
                        .content(json(vacation));

        this.mockMvc.perform(builder)
                .andExpect(status().isNotFound());

        List<Vacation> all = repository.findAll();
        assertEquals(1, all.size());
    }

    @Test
    public void deleteVacation() throws Exception {

        MockHttpServletRequestBuilder builder =
                MockMvcRequestBuilders.delete(VACATION_URI + vacation.getId())
                        .contentType(contentType);

        this.mockMvc.perform(builder)
                .andExpect(status().isOk());

        List<Vacation> all = repository.findAll();
        assertEquals(0, all.size());
    }

    @Test
    public void deleteVacationNotFound() throws Exception {

        MockHttpServletRequestBuilder builder =
                MockMvcRequestBuilders.delete(VACATION_URI + 2)
                        .contentType(contentType);

        this.mockMvc.perform(builder)
                .andExpect(status().isNotFound());

        List<Vacation> all = repository.findAll();
        assertEquals(1, all.size());
    }


    @Before
    public void setUp() throws Exception {
        this.repository.deleteAllInBatch();
        vacation = repository.save(createVacation("01.02.2018", "10.02.2018", "Пупкин Василий Иванович"));
    }

    @Autowired
    void setConverters(HttpMessageConverter<?>[] converters) {

        this.mappingJackson2HttpMessageConverter = Arrays.stream(converters)
                .filter(hmc -> hmc instanceof MappingJackson2HttpMessageConverter)
                .findAny()
                .orElse(null);

        assertNotNull("the JSON message converter must not be null",
                this.mappingJackson2HttpMessageConverter);
    }

    private Vacation createVacation(String begin, String end, String name) throws ParseException {
        vacation = new Vacation();
        vacation.setName(name);
        vacation.setStart(fromString(begin));
        vacation.setEnd(fromString(end));
        vacation.setStatus(VacationStatus.NEW);
        return vacation;
    }

    private Date fromString(String str) throws ParseException {
        DateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
        return formatter.parse(str);
    }

    private String json(Object o) throws IOException {
        MockHttpOutputMessage mockHttpOutputMessage = new MockHttpOutputMessage();
        this.mappingJackson2HttpMessageConverter.write(
                o, MediaType.APPLICATION_JSON, mockHttpOutputMessage);
        return mockHttpOutputMessage.getBodyAsString();
    }

}