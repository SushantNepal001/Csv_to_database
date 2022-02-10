package com.batch.csvtomysql.config;

import com.batch.csvtomysql.entity.User;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.expression.spel.ast.FloatLiteral;

import javax.sql.DataSource;

@Configuration
@EnableBatchProcessing
public class BatchConfig {
    @Autowired
    private DataSource dataSource;

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    //item reader ko subclass ho uo
    @Bean
    public FlatFileItemReader<User> reader() {
        FlatFileItemReader<User> reader = new FlatFileItemReader<>();
        reader.setResource(new ClassPathResource("MOCK_DATA.csv"));
        reader.setLineMapper(getLineMapper());
        reader.setLinesToSkip(1);
        return reader;

    }

    //yo chahe reader ho
    private LineMapper<User> getLineMapper() {
        DefaultLineMapper<User> lineMapper = new DefaultLineMapper<>();

        DelimitedLineTokenizer lineTokenizer = new DelimitedLineTokenizer();
        lineTokenizer.setNames(new String[]{"userId", "first_name", "last_name", "email", "gender"});
        lineTokenizer.setIncludedFields(new int[]{0, 1, 2, 3,4});


        BeanWrapperFieldSetMapper<User> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
        fieldSetMapper.setTargetType(User.class);
        //tokenizer ley comm ley separate gardiney capability huncha
        lineMapper.setLineTokenizer(lineTokenizer);
        lineMapper.setFieldSetMapper(fieldSetMapper);

        return lineMapper;
    }

    //up chaheh processor ho!
    @Bean
    public UserItemProcessor processor() {
        return new UserItemProcessor();
    }


    //yo chahe writer ho
    @Bean
    public JdbcBatchItemWriter<User> writer() {
        JdbcBatchItemWriter<User> writer = new JdbcBatchItemWriter<>();
        writer.setItemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<User>());
        writer.setSql("insert into user(id,firstName,lastName,email,gender) values(:id,:first_name,:last_name,:email,:gender)");
        writer.setDataSource(this.dataSource);
        return writer;
    }

    //job banairako
    @Bean
    public Job importUserJob() {

        return this.jobBuilderFactory.get("USER-IMPORT-JOB")
                .incrementer(new RunIdIncrementer())
                .flow(step1())
                .end()
                .build();


    }

    public Step step1() {
        return this.stepBuilderFactory.get("step1")
                .<User,User>chunk(10)
                .reader(reader())
                .processor(processor())
                .writer(writer())
                .build();
    }

}
