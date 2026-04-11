-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Apr 11, 2026 at 07:11 PM
-- Server version: 10.4.32-MariaDB
-- PHP Version: 8.0.30

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `smm_panel_db`
--

-- --------------------------------------------------------

--
-- Table structure for table `buff_orders`
--

CREATE TABLE `buff_orders` (
  `id` bigint(20) NOT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `current_count` int(11) NOT NULL,
  `initial_count` int(11) NOT NULL,
  `price` decimal(15,2) DEFAULT NULL,
  `quantity` int(11) NOT NULL,
  `status` enum('CANCELLED','COMPLETED','IN_PROGRESS','PENDING') DEFAULT NULL,
  `target_link` varchar(500) NOT NULL,
  `user_id` bigint(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `contact_messages`
--

CREATE TABLE `contact_messages` (
  `id` bigint(20) NOT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `is_processed` bit(1) NOT NULL,
  `message` text DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `phone` varchar(255) DEFAULT NULL,
  `user_id` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `contact_messages`
--

INSERT INTO `contact_messages` (`id`, `created_at`, `email`, `is_processed`, `message`, `name`, `phone`, `user_id`) VALUES
(4, '2026-04-08 12:39:27.000000', 'son0356435855@gmail.com', b'0', '123', 'TRAN VAN THAI SON', '0354990677', 4),
(5, '2026-04-08 12:41:47.000000', 'son0356435855@gmail.com', b'0', 'cứu tôi', 'TRAN VAN THAI SON', '0354990677', 4),
(6, '2026-04-08 12:42:01.000000', 'ngotantai18092004@gmail.com', b'0', 'dsfsdaf', 'TRAN VAN THAI SON', '0354990677', 4);

-- --------------------------------------------------------

--
-- Table structure for table `notifications`
--

CREATE TABLE `notifications` (
  `id` bigint(20) NOT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `is_read` bit(1) NOT NULL,
  `message` varchar(1000) NOT NULL,
  `user_id` bigint(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `recharge_requests`
--

CREATE TABLE `recharge_requests` (
  `id` bigint(20) NOT NULL,
  `amount` decimal(15,2) NOT NULL,
  `content` varchar(255) DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `status` enum('APPROVED','PENDING','REJECTED') DEFAULT NULL,
  `user_id` bigint(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `service_settings`
--

CREATE TABLE `service_settings` (
  `id` bigint(20) NOT NULL,
  `base_price` decimal(15,2) NOT NULL,
  `default_quantity` int(11) NOT NULL,
  `service_name` varchar(255) NOT NULL,
  `platform` varchar(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `users`
--

CREATE TABLE `users` (
  `id` bigint(20) NOT NULL,
  `balance` decimal(15,2) DEFAULT NULL,
  `email` varchar(255) NOT NULL,
  `full_name` varchar(255) DEFAULT NULL,
  `is_locked` bit(1) NOT NULL,
  `password` varchar(255) NOT NULL,
  `role` enum('ADMIN','CUSTOMER') DEFAULT NULL,
  `username` varchar(255) NOT NULL,
  `recharge_code` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `users`
--

INSERT INTO `users` (`id`, `balance`, `email`, `full_name`, `is_locked`, `password`, `role`, `username`, `recharge_code`) VALUES
(4, 0.00, 'son0356435855@gmail.com', 'TRAN VAN THAI SON', b'0', '$2a$10$8xCUFUgr/dl8oJcJd/C2gegfNmfbFMIND0Zf7r0HZh3.UW.CGFz8C', 'CUSTOMER', 'user1', 'NAP9E1086BF');

--
-- Indexes for dumped tables
--

--
-- Indexes for table `buff_orders`
--
ALTER TABLE `buff_orders`
  ADD PRIMARY KEY (`id`),
  ADD KEY `FK4xqh3jtvvqs07onfsk7iqyxwv` (`user_id`);

--
-- Indexes for table `contact_messages`
--
ALTER TABLE `contact_messages`
  ADD PRIMARY KEY (`id`),
  ADD KEY `FKj305kltauaydco2n00yr55nbk` (`user_id`);

--
-- Indexes for table `notifications`
--
ALTER TABLE `notifications`
  ADD PRIMARY KEY (`id`),
  ADD KEY `FK9y21adhxn0ayjhfocscqox7bh` (`user_id`);

--
-- Indexes for table `recharge_requests`
--
ALTER TABLE `recharge_requests`
  ADD PRIMARY KEY (`id`),
  ADD KEY `FK2xy3ve617qbkert4l0fefpuim` (`user_id`);

--
-- Indexes for table `service_settings`
--
ALTER TABLE `service_settings`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `UK6dotkott2kjsp8vw4d0m25fb7` (`email`),
  ADD UNIQUE KEY `UKr43af9ap4edm43mmtq01oddj6` (`username`),
  ADD UNIQUE KEY `UK26xs78a76o8aov6g6etljv7nx` (`recharge_code`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `buff_orders`
--
ALTER TABLE `buff_orders`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT for table `contact_messages`
--
ALTER TABLE `contact_messages`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=7;

--
-- AUTO_INCREMENT for table `notifications`
--
ALTER TABLE `notifications`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=7;

--
-- AUTO_INCREMENT for table `recharge_requests`
--
ALTER TABLE `recharge_requests`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `service_settings`
--
ALTER TABLE `service_settings`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=34;

--
-- AUTO_INCREMENT for table `users`
--
ALTER TABLE `users`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `buff_orders`
--
ALTER TABLE `buff_orders`
  ADD CONSTRAINT `FK4xqh3jtvvqs07onfsk7iqyxwv` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`);

--
-- Constraints for table `contact_messages`
--
ALTER TABLE `contact_messages`
  ADD CONSTRAINT `FKj305kltauaydco2n00yr55nbk` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`);

--
-- Constraints for table `notifications`
--
ALTER TABLE `notifications`
  ADD CONSTRAINT `FK9y21adhxn0ayjhfocscqox7bh` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`);

--
-- Constraints for table `recharge_requests`
--
ALTER TABLE `recharge_requests`
  ADD CONSTRAINT `FK2xy3ve617qbkert4l0fefpuim` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
