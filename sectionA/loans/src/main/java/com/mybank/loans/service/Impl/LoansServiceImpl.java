package com.mybank.loans.service.Impl;

import java.util.Optional;
import java.util.Random;

import org.springframework.stereotype.Service;

import com.mybank.loans.constants.LoansConstants;
import com.mybank.loans.dto.LoansDto;
import com.mybank.loans.entity.Loans;
import com.mybank.loans.exception.LoanAlreadyExistsException;
import com.mybank.loans.exception.ResourceNotFoundException;
import com.mybank.loans.mapper.LoansMapper;
import com.mybank.loans.repository.LoansRepository;
import com.mybank.loans.service.ILoansService;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class LoansServiceImpl implements ILoansService {

	private LoansRepository loansRepository;

	@Override
	public void createLoan(String mobileNumber) {
		Optional<Loans> optionalLoans = loansRepository.findByMobileNumber(mobileNumber);
		if (optionalLoans.isPresent()) {
			throw new LoanAlreadyExistsException("Loan already registered with given mobileNumber " + mobileNumber);
		}
		loansRepository.save(createNewLoan(mobileNumber));
	}

	private Loans createNewLoan(String mobileNumber) {
		Loans newLoan = new Loans();
		long randomLoanNumber = 100000000000L + new Random().nextInt(900000000);
		newLoan.setLoanNumber(Long.toString(randomLoanNumber));
		newLoan.setMobileNumber(mobileNumber);
		newLoan.setLoanType(LoansConstants.HOME_LOAN);
		newLoan.setTotalLoan(LoansConstants.NEW_LOAN_LIMIT);
		newLoan.setAmountPaid(0);
		newLoan.setOutstandingAmount(LoansConstants.NEW_LOAN_LIMIT);
		return newLoan;
	}

	@Override
	public LoansDto fetchLoan(String mobileNumber) {
		Loans loans = loansRepository.findByMobileNumber(mobileNumber)
				.orElseThrow(() -> new ResourceNotFoundException("Loan", "mobileNumber", mobileNumber));
		return LoansMapper.mapToLoansDto(loans, new LoansDto());
	}

	@Override
	public boolean updateLoan(LoansDto loansDto) {
		Loans loans = loansRepository.findByLoanNumber(loansDto.getLoanNumber())
				.orElseThrow(() -> new ResourceNotFoundException("Loan", "LoanNumber", loansDto.getLoanNumber()));
		LoansMapper.mapToLoans(loansDto, loans);
		loansRepository.save(loans);
		return true;
	}

	@Override
	public boolean deleteLoan(String mobileNumber) {
		Loans loans = loansRepository.findByMobileNumber(mobileNumber)
				.orElseThrow(() -> new ResourceNotFoundException("Loan", "mobileNumber", mobileNumber));
		loansRepository.deleteById(loans.getLoanId());
		return true;
	}

}
