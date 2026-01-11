package com.example.magazyn.service;

import com.example.magazyn.entity.Alert;
import com.example.magazyn.entity.Company;
import com.example.magazyn.entity.Product;
import com.example.magazyn.repository.AlertRepository;
import com.example.magazyn.repository.CompanyRepository;
import com.example.magazyn.repository.ProductRepository;
import com.example.magazyn.repository.StockItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AlertService {

    private final AlertRepository alertRepository;
    private final ProductRepository productRepository;
    private final StockItemRepository stockItemRepository;
    private final CompanyRepository companyRepository;

    private static final int LOW_STOCK_THRESHOLD = 10;

    @Transactional(readOnly = true)
    public List<Alert> getUnreadAlertsForCompany(Company company) {
        return alertRepository.findByCompanyAndIsReadOrderByTimestampDesc(company, false);
    }

    @Transactional
    public void markAlertAsRead(Long alertId, Company company) {
        alertRepository.findById(alertId)
                .filter(alert -> alert.getCompany().getId().equals(company.getId()))
                .ifPresent(alert -> {
                    alert.setRead(true);
                    alertRepository.save(alert);
                });
    }

    @Transactional
    public void checkAndGenerateAllAlerts() {
        List<Company> companies = companyRepository.findAll();
        for (Company company : companies) {
            checkLowStockAlerts(company);
        }
    }

    private void checkLowStockAlerts(Company company) {
        List<Product> products = productRepository.findAllByCompany(company);
        for (Product product : products) {
            Integer totalQuantity = stockItemRepository.sumQuantityByProductId(product.getId());
            int currentStock = (totalQuantity != null) ? totalQuantity : 0;

            if (currentStock > 0 && currentStock <= LOW_STOCK_THRESHOLD) {
                String msg = String.format("Niski stan magazynowy dla produktu '%s' (ID: %d). Aktualna ilość: %d (Próg: %d).",
                        product.getName(), product.getId(), currentStock, LOW_STOCK_THRESHOLD);
                createAndSaveAlert(Alert.AlertType.LOW_STOCK, msg, company);
            }
        }
    }

    private void createAndSaveAlert(Alert.AlertType type, String message, Company company) {
        Alert alert = new Alert();
        alert.setType(type);
        alert.setMessage(message);
        alert.setCompany(company);
        alert.setTimestamp(LocalDateTime.now());
        alert.setRead(false);
        alertRepository.save(alert);
    }
}