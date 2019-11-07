package com.gingernet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gingernet.word.WordList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;
import java.util.Random;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = WalletJavaSdkApplication.class,webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@Ignore
public class WalletJavaSdkApplicationTest {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private WebApplicationContext context;

    @LocalServerPort
    private int port;

    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static final char[] codString = new char[]{'q','w','e','r','t','y','u','i','o','p','a','s','d','f','g','h','j','k','l',
            'z','x','c','v','b','n','m','!','@','#','$','%','&','*'};

    private MockMvc mockMvc;
    private RestTemplate restTemplate = new RestTemplate();

    @Before
    public void setupMockMvc() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    public void contextLoads() {
    }

    public static String getRandomCodeString(Integer count){
        Random random = new Random();
        StringBuffer stringBuffer= new StringBuffer();
        for (int i=0;i<count;i++){
            stringBuffer.append(codString[random.nextInt(codString.length)]);
        }
        return stringBuffer.toString();
    }

    public static String getRandomCode(Integer count){
        Random random = new Random();
        String result="";
        for (int i=0;i<count;i++){
            result+=random.nextInt(10);
        }
        return result;
    }

    @Test
    public void testCreatWord() throws Exception {
        WordList wordList = new WordList();
        List<String> word = wordList.CreateWord();
        System.out.println(word);
    }

}
