package com.amigoscode.testing.customer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;


class CustomerRegistrationServiceTest {
    private CustomerRegistrationService underTest;
    @Mock
    private CustomerRepository customerRepository;
    @Captor
    private ArgumentCaptor<Customer> customerArgumentCaptor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        underTest = new CustomerRegistrationService(customerRepository);
    }

    @Test
    void itShouldSaveRegisterNewCustomer() {
        // Given
        String phoneNumber = "000099";
        Customer customer = new Customer(UUID.randomUUID(), "Maryam", phoneNumber);

        // ... a request
        CustomerRegistrationRequest customerRegistrationRequest = new CustomerRegistrationRequest(customer);

        // ... No customer with phone number passed
        given(customerRepository.selectCustomerByPhoneNumber(phoneNumber)).willReturn(Optional.empty());
        // When
        underTest.registerNewCustomer(customerRegistrationRequest);

        // Then
        then(customerRepository).should().save(customerArgumentCaptor.capture());
        Customer argumentCaptorValue = customerArgumentCaptor.getValue();
        assertThat(argumentCaptorValue).usingRecursiveComparison().isEqualTo(customer);
    }

    @Test
    void itShouldNotSaveRegisterNewCustomer() {
        // Given
        String phoneNumber = "000099";
        Customer customer = new Customer(UUID.randomUUID(), "Maryam", phoneNumber);

        // ... a request
        CustomerRegistrationRequest customerRegistrationRequest = new CustomerRegistrationRequest(customer);

        // ... an existing customer is return
        given(customerRepository.selectCustomerByPhoneNumber(phoneNumber)).willReturn(Optional.of(customer));
        // When
        underTest.registerNewCustomer(customerRegistrationRequest);
        // Then

        // 调用了 selectCustomerByPhoneNumber
        then(customerRepository).should().selectCustomerByPhoneNumber(phoneNumber);
        // 没有调用 save 方法； 可以使用下面的方式验证，因此这个可以去掉
        // then(customerRepository).should(never()).save(any());
        // 没有调用其它方法
        then(customerRepository).shouldHaveNoMoreInteractions();
    }

    @Test
    void itShouldThrowWhenPhoneNumberIsTaken() {
        // Given
        String phoneNumber = "000099";
        Customer customer = new Customer(UUID.randomUUID(), "Maryam", phoneNumber);
        Customer customerTwo = new Customer(UUID.randomUUID(), "John", phoneNumber);

        // ... a request
        CustomerRegistrationRequest request = new CustomerRegistrationRequest(customer);

        // ... an existing customer is return
        given(customerRepository.selectCustomerByPhoneNumber(phoneNumber)).willReturn(Optional.of(customerTwo));

        // When
        // Then
        assertThatThrownBy(() -> underTest.registerNewCustomer(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("phone number");

        then(customerRepository).should().selectCustomerByPhoneNumber(any());
        then(customerRepository).should(never()).save(any());
    }


    @Test
    void itShouldThrowWhenPhoneNumberIdIsNull() {
        // Given
        String phoneNumber = "000099";
        Customer customer = new Customer(null, "Maryam", phoneNumber);

        // ... a request
        CustomerRegistrationRequest request = new CustomerRegistrationRequest(customer);

        // ... an existing customer is return
        given(customerRepository.selectCustomerByPhoneNumber(phoneNumber)).willReturn(Optional.empty());

        // When
        underTest.registerNewCustomer(request);

        // Then
        then(customerRepository).should().save(customerArgumentCaptor.capture());
        Customer customerArgumentCaptorValue = customerArgumentCaptor.getValue();
        assertThat(customerArgumentCaptorValue).isEqualToIgnoringGivenFields(customer, "id");
        assertThat(customerArgumentCaptorValue.getId()).isNotNull();
    }
}