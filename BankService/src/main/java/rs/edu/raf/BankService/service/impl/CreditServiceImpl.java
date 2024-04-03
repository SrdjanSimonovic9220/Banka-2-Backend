package rs.edu.raf.BankService.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import rs.edu.raf.BankService.data.dto.CreditDto;
import rs.edu.raf.BankService.data.dto.CreditRequestDto;
import rs.edu.raf.BankService.data.entities.Account;
import rs.edu.raf.BankService.data.entities.Credit;
import rs.edu.raf.BankService.data.entities.CreditRequest;
import rs.edu.raf.BankService.data.enums.CreditRequestStatus;
import rs.edu.raf.BankService.mapper.CreditMapper;
import rs.edu.raf.BankService.repository.AccountRepository;
import rs.edu.raf.BankService.repository.credit.CreditRepository;
import rs.edu.raf.BankService.repository.credit.CreditRequestRepository;
import rs.edu.raf.BankService.service.CreditService;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class CreditServiceImpl implements CreditService {
    private final CreditRepository creditRepository;
    private final CreditRequestRepository creditRequestRepository;
    private final CreditMapper creditMapper;
    private final AccountRepository accountRepository;

    @Override
    public CreditDto createCredit(Credit credit) {
        Credit credit1 = creditRepository.findCreditByCreditNumber(credit.getCreditNumber());
        if (credit1 != null) {
            throw new RuntimeException("Credit already exists");
        }
        credit = creditRepository.save(credit);
        return creditMapper.creditToCreditDto(credit);
    }

    @Override
    public List<CreditDto> getCreditsByAccountNumber(String accountNumber) {
        return creditRepository.findAllByAccountNumber(accountNumber).stream().map(creditMapper::creditToCreditDto).toList();
    }

    @Override
    public CreditDto getCreditByCreditNumber(Long creditNumber) {
        return creditMapper.creditToCreditDto(creditRepository.findCreditByCreditNumber(creditNumber));
    }

    @Override
    public CreditRequestDto createCreditRequest(CreditRequestDto creditRequestDto) {
        CreditRequest creditRequest = creditMapper.creditRequestDtoToCreditRequest(creditRequestDto);
        creditRequest = creditRequestRepository.save(creditRequest);
        return creditMapper.creditRequestToCreditRequestDto(creditRequest);
    }

    @Override
    public List<CreditRequestDto> getAllCreditRequests() {
        return creditRequestRepository.findAll().stream().map(creditMapper::creditRequestToCreditRequestDto).toList();
    }

    @Override
    public CreditDto approveCreditRequest(Long creditRequestId) {
        CreditRequest creditRequest = creditRequestRepository.findById(creditRequestId).orElseThrow();
        if (creditRequest.getStatus() != CreditRequestStatus.PENDING) {
            throw new RuntimeException("Credit request is not pending");
        }
        Account account = accountRepository.findByAccountNumber(creditRequest.getAccountNumber());
        if (account == null) {
            throw new RuntimeException("Account not found");
        }

        creditRequest.setStatus(CreditRequestStatus.APPROVED);
        creditRequest = creditRequestRepository.save(creditRequest);
        CreditDto creditDto = new CreditDto();
        // Create credit ovo je hardkodovano otprilike, jer je specifikacija losa, i ne treba inace ovako da se radi ali tako pise u spec otp
        //ako se spec promeni (a damjanovic je rekao da nece jer je gr4 vec uradila) onda menjati ovaj deo
        creditDto.setCurrencyCode(creditRequest.getCurrency());
        creditDto.setAccountNumber(creditRequest.getAccountNumber());
        creditDto.setCreditCreationDate(LocalDateTime.now().toEpochSecond(ZoneOffset.UTC));
        creditDto.setCreditName(String.valueOf(creditRequest.getCreditType()));
        creditDto.setPaymentPeriodMonths(creditRequest.getPaymentPeriodMonths());
        creditDto.setNominalInterestRate(new Random().nextDouble(5, 10));
        creditDto.setEffectiveInterestRate(new Random().nextDouble(creditDto.getNominalInterestRate(), creditDto.getNominalInterestRate() + 2));
        creditDto.setCreditAmount(creditRequest.getCreditAmount());
        creditDto.setRemainingAmount(creditDto.getCreditAmount() * (1 + (creditDto.getEffectiveInterestRate() / 100)));
        creditDto.setInstallmentAmount(creditDto.getRemainingAmount() / creditDto.getPaymentPeriodMonths());
        creditDto.setNextInstallmentDate(LocalDateTime.now().plusMonths(1).toEpochSecond(ZoneOffset.UTC));
        creditDto.setCreditExpirationDate(LocalDateTime.now().plusMonths(creditDto.getPaymentPeriodMonths()).toEpochSecond(ZoneOffset.UTC));


        return createCredit(creditMapper.creditDtoToCredit(creditDto));
    }

    @Override
    public Boolean denyCreditRequest(Long creditRequestId) {
        CreditRequest creditRequest = creditRequestRepository.findById(creditRequestId).orElseThrow();
        if (creditRequest.getStatus() != CreditRequestStatus.PENDING) {
            return false;
        }
        creditRequest.setStatus(CreditRequestStatus.REJECTED);
        creditRequestRepository.save(creditRequest);
        return true;
    }


}