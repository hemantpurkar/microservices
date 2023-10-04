package com.mybank.accounts.service.impl;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.mybank.accounts.dto.AccountsDto;
import com.mybank.accounts.dto.CardsDto;
import com.mybank.accounts.dto.CustomerDetailsDto;
import com.mybank.accounts.dto.LoansDto;
import com.mybank.accounts.entity.Accounts;
import com.mybank.accounts.entity.Customer;
import com.mybank.accounts.exception.ResourceNotFoundException;
import com.mybank.accounts.mapper.AccountsMapper;
import com.mybank.accounts.mapper.CustomerMapper;
import com.mybank.accounts.repository.AccountsRepository;
import com.mybank.accounts.repository.CustomerRepository;
import com.mybank.accounts.service.ICustomersService;
import com.mybank.accounts.service.client.CardsFeignClient;
import com.mybank.accounts.service.client.LoansFeignClient;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class CustomersServiceImpl implements ICustomersService {

	private AccountsRepository accountsRepository;
	private CustomerRepository customerRepository;
	private CardsFeignClient cardsFeignClient;
	private LoansFeignClient loansFeignClient;

	/**
	 * @param mobileNumber - Input Mobile Number
	 * @return Customer Details based on a given mobileNumber
	 */
	@Override
	public CustomerDetailsDto fetchCustomerDetails(String mobileNumber, String correlationId) {
		Customer customer = customerRepository.findByMobileNumber(mobileNumber)
				.orElseThrow(() -> new ResourceNotFoundException("Customer", "mobileNumber", mobileNumber));
		Accounts accounts = accountsRepository.findByCustomerId(customer.getCustomerId()).orElseThrow(
				() -> new ResourceNotFoundException("Account", "customerId", customer.getCustomerId().toString()));

		CustomerDetailsDto customerDetailsDto = CustomerMapper.mapToCustomerDetailsDto(customer,
				new CustomerDetailsDto());
		customerDetailsDto.setAccountsDto(AccountsMapper.mapToAccountsDto(accounts, new AccountsDto()));

		ResponseEntity<LoansDto> loansDtoResponseEntity = loansFeignClient.fetchLoanDetails(correlationId, mobileNumber);
		customerDetailsDto.setLoansDto(loansDtoResponseEntity.getBody());

		ResponseEntity<CardsDto> cardsDtoResponseEntity = cardsFeignClient.fetchCardDetails(correlationId, mobileNumber);
		customerDetailsDto.setCardsDto(cardsDtoResponseEntity.getBody());

		return customerDetailsDto;

	}
}
