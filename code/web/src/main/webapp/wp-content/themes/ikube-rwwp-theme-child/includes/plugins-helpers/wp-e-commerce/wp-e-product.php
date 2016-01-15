<?php
class Wp_E_Product
{
    public $id;
    public $title;
    public $description;
    public $additional_description;
    public $permalink;
    public $regular_price;
    public $sale_price;
    public $thumbnail;
    public $categories;

    public function get_actual_price()
    {
        $this->check_for_id();
        if(!is_numeric($this->regular_price))
            return $this->sale_price;

        if(!is_numeric($this->sale_price))
            return $this->regular_price;

        if($this->sale_price <= $this->regular_price)
            return $this->sale_price;
        else
            return $this->regular_price;
    }

    public function get_save_percentage()
    {
        $this->check_for_id();
        $price_save_params = $this->price_save_params("percentage");
        return wpsc_you_save( $price_save_params );
    }

    public function get_save_amount()
    {
        $this->check_for_id();
        $price_save_params = $this->price_save_params("amount");
        return wpsc_you_save( $price_save_params );
    }

    private function check_for_id()
    {
        if($this->id <= 0)
            throw new Exception('Object id is not correct');
    }

    private function get_price_save_params($type)
    {
        $price_save_params = array(
            'product_id' => $this->id,
            'type' => $type,
            'variations' => false
        );
        return $price_save_params;
    }
}